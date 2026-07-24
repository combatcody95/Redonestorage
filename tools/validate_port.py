#!/usr/bin/env python3
from pathlib import Path
import json, re, sys, zipfile

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
JAVA = ROOT / 'src/main/java'
RS_JAR = ROOT / 'libs/refinedstorage-neoforge-2.0.9.jar'

ITEMS = {
    'small_item_disk_part','medium_item_disk_part','large_item_disk_part','larger_item_disk_part',
    'small_fluid_disk_part','medium_fluid_disk_part','large_fluid_disk_part','larger_fluid_disk_part',
    'small_item_disk','medium_item_disk','large_item_disk','larger_item_disk',
    'small_fluid_disk','medium_fluid_disk','large_fluid_disk','larger_fluid_disk',
    'raw_super_advanced_processor','super_advanced_processor',
    'super_wireless_crafting_grid','creative_super_wireless_crafting_grid',
    'multiblock_frame','multiblock_heat','multiblock_cpu','multiblock_storage',
    'advanced_wireless_transmitter'
}
BLOCKS = {'multiblock_frame','multiblock_heat','multiblock_cpu','multiblock_storage','advanced_wireless_transmitter'}
errors=[]

def fail(msg): errors.append(msg)

# Parse every JSON resource.
for f in list(RES.rglob('*.json')) + [RES/'pack.mcmeta']:
    try: json.loads(f.read_text(encoding='utf-8'))
    except Exception as e: fail(f'invalid JSON: {f.relative_to(ROOT)}: {e}')

# 1.21.1 resource path and namespace checks.
for bad in ('recipes','loot_tables'):
    for d in (RES/'data').rglob(bad): fail(f'obsolete plural data directory: {d.relative_to(ROOT)}')
for f in (RES/'data').rglob('*.json'):
    text=f.read_text(encoding='utf-8')
    if 'forge:' in text: fail(f'obsolete forge tag in {f.relative_to(ROOT)}')
    if '4096k_fluid_storage_part' in text: fail(f'obsolete RS1 fluid part in {f.relative_to(ROOT)}')
    if 'refinedstorage:crafting_upgrade' in text: fail(f'obsolete RS1 crafting upgrade in {f.relative_to(ROOT)}')

# Connected-texture metadata must point at real texture sprites.
for metadata in (RES/'assets').rglob('*.png.mcmeta'):
    try:
        data=json.loads(metadata.read_text(encoding='utf-8'))
    except Exception:
        continue
    for texture in data.get('ctm', {}).get('textures', []):
        namespace, sep, path = texture.partition(':')
        if not sep:
            namespace='minecraft'
            path=texture
        target=RES/f'assets/{namespace}/textures/{path}.png'
        if not target.is_file():
            fail(f'CTM metadata {metadata.relative_to(ROOT)} references missing texture {texture}')

# Registered content has models, translations, blockstates and loot.
lang=json.loads((RES/'assets/redonestorage/lang/en_us.json').read_text())
for item in sorted(ITEMS):
    if not (RES/f'assets/redonestorage/models/item/{item}.json').is_file(): fail(f'missing item model: {item}')
    key=('block' if item in BLOCKS else 'item')+f'.redonestorage.{item}'
    if key not in lang: fail(f'missing en_us translation: {key}')
for block in sorted(BLOCKS):
    if not (RES/f'assets/redonestorage/blockstates/{block}.json').is_file(): fail(f'missing blockstate: {block}')
    if not (RES/f'data/redonestorage/loot_table/blocks/{block}.json').is_file(): fail(f'missing loot table: {block}')

# Recipe outputs and Redone Storage inputs refer to registered content; RS inputs exist in the supplied JAR.
with zipfile.ZipFile(RS_JAR) as z:
    jar_names=set(z.namelist())
    jar_flat='\n'.join(jar_names)
for f in (RES/'data/redonestorage/recipe').rglob('*.json'):
    data=json.loads(f.read_text())
    def walk(v, parent=None):
        if isinstance(v,dict):
            for k,x in v.items():
                if k in ('item','id') and isinstance(x,str):
                    ns,_,name=x.partition(':')
                    if ns=='redonestorage' and name not in ITEMS: fail(f'unknown Redone Storage ID {x} in {f.relative_to(ROOT)}')
                    if k=='item' and ns=='refinedstorage':
                        markers=(f'data/refinedstorage/recipe/{name}.json', f'assets/refinedstorage/models/item/{name}.json')
                        if not any(m in jar_names for m in markers): fail(f'RS2 item not found in supplied jar: {x} ({f.relative_to(ROOT)})')
                walk(x,k)
        elif isinstance(v,list):
            for x in v: walk(x,parent)
    walk(data)

# Redone Storage must never define classes in Refined Storage's namespace.
for f in JAVA.rglob('*.java'):
    text=f.read_text(encoding='utf-8')
    package_match=re.search(r'^package\s+([\w.]+);', text, re.M)
    if package_match and package_match.group(1).startswith('com.refinedmods.refinedstorage'):
        fail(f'foreign Refined Storage package declared by {f.relative_to(ROOT)}: {package_match.group(1)}')

# The multiblock must be visible to RS2's Autocrafter Manager and use the real GUI sheet.
autocrafter_bridge = JAVA / 'net/gigabit101/redonestorage/multiblock/MultiblockAutocrafterNetworkNodeContainer.java'
if not autocrafter_bridge.is_file():
    fail('missing RS2 Autocrafter Manager bridge')
else:
    bridge_text = autocrafter_bridge.read_text(encoding='utf-8')
    if 'implements Autocrafter' not in bridge_text:
        fail('multiblock manager bridge does not implement Autocrafter')

screen = JAVA / 'net/gigabit101/redonestorage/client/MultiblockCrafterScreen.java'
if screen.is_file():
    screen_text = screen.read_text(encoding='utf-8')
    if 'textures/gui/gui_sheet.png' not in screen_text:
        fail('multiblock screen is not using the Redone Storage GUI sheet')
    if 'graphics.fill(' in screen_text:
        fail('multiblock screen still contains placeholder flat-color rendering')

# Every RS import is either supplied by the jar or implemented as a local bridge.
with zipfile.ZipFile(RS_JAR) as z: classes=set(z.namelist())
for f in JAVA.rglob('*.java'):
    text=f.read_text(encoding='utf-8')
    for imp in re.findall(r'^import\s+(com\.refinedmods\.[\w.]+);', text, re.M):
        class_path=imp.replace('.','/')+'.class'
        source_path=JAVA/(imp.replace('.','/')+'.java')
        if class_path not in classes and not source_path.is_file(): fail(f'missing Refined Storage class {imp}, imported by {f.relative_to(ROOT)}')

if errors:
    print('PORT VALIDATION FAILED')
    for e in errors: print(' -',e)
    sys.exit(1)
print(f'PORT STATIC VALIDATION PASSED: {len(ITEMS)} items, {len(BLOCKS)} blocks, '
      f'{len(list((RES/"data/redonestorage/recipe").rglob("*.json")))} recipes')
