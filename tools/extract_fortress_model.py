#!/usr/bin/env python3
"""Mechanical TC4 ModelFortressArmor -> modern CSV conversion."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
source = (ROOT / "reference/Thaumcraft-4.2-FOREVA-master/src/main/java/thaumcraft/client/renderers/models/gear/ModelFortressArmor.java").read_text()
target = ROOT / "src/main/resources/assets/thaumic_reborn/models/entity/fortress_armor.csv"

created = {}
for match in re.finditer(r"this\.(\w+) = new ModelRenderer\(this,\s*(\d+),\s*(\d+)\);", source):
    created[match.group(1)] = {"u": match.group(2), "v": match.group(3), "mirror": "false"}
for name in created:
    if re.search(rf"this\.{name}\.mirror = true;", source):
        created[name]["mirror"] = "true"
    box = re.search(rf"this\.{name}\.addBox\(([^;]+)\);", source)
    pivot = re.search(rf"this\.{name}\.setRotationPoint\(([^;]+)\);", source)
    rotation = re.search(rf"this\.setRotation\(this\.{name},\s*([^;]+)\);", source)
    if box and pivot and rotation:
        created[name]["box"] = [x.strip() for x in box.group(1).split(",")]
        created[name]["pivot"] = [x.strip() for x in pivot.group(1).split(",")]
        created[name]["rotation"] = [x.strip() for x in rotation.group(1).split(",")]

parents = {}
for parent, child in re.findall(r"this\.(bipedHead|bipedBody|bipedRightArm|bipedLeftArm|bipedRightLeg|bipedLeftLeg)\.addChild\(this\.(\w+)\);", source):
    parents[child] = {
        "bipedHead": "head", "bipedBody": "body", "bipedRightArm": "rightArm",
        "bipedLeftArm": "leftArm", "bipedRightLeg": "rightLeg", "bipedLeftLeg": "leftLeg",
    }[parent]

rows = ["# parent,name,u,v,mirror,x,y,z,dx,dy,dz,pivotX,pivotY,pivotZ,rotX,rotY,rotZ"]
for name, part in created.items():
    if name not in parents or not all(k in part for k in ("box", "pivot", "rotation")):
        continue
    values = [parents[name], name, part["u"], part["v"], part["mirror"], *part["box"], *part["pivot"], *part["rotation"]]
    rows.append(",".join(values))

# The three masks are built in an old Java loop and therefore added explicitly.
for index, u in enumerate((52, 76, 100)):
    rows.append(f"head,Mask{index},{u},2,false,-4.5f,-5.0f,-4.6f,9,5,1,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f")

target.write_text("\n".join(rows) + "\n")
print(f"wrote {target} ({len(rows)-1} cuboids)")
