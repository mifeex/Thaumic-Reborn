#!/usr/bin/env python3
"""Reconstruct the TC4 Runic Matrix runtime mesh as a modern OBJ.

Thaumcraft 4.2.3.5 does not ship an OBJ for the Runic Matrix. Its
TileRunicMatrixRenderer renders eight rotated ModelCube(0) instances at the
corners of the block, each scaled to 0.45. This exporter preserves that source
geometry and the 64x64 ModelRenderer UV layout in a deterministic OBJ.
"""

from __future__ import annotations

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = (
    ROOT
    / "src/main/resources/assets/thaumic_reborn/textures/models"
    / "runic_matrix.obj"
)

VERTICES = (
    (-1, -1, -1),
    (1, -1, -1),
    (1, 1, -1),
    (-1, 1, -1),
    (-1, -1, 1),
    (1, -1, 1),
    (1, 1, 1),
    (-1, 1, 1),
)

# Counter-clockwise when viewed from outside.
FACES = (
    ((0, 3, 2, 1), (0.25, 0.25, 0.50, 0.50)),  # north
    ((5, 6, 7, 4), (0.75, 0.25, 1.00, 0.50)),  # south
    ((4, 7, 3, 0), (0.00, 0.25, 0.25, 0.50)),  # west
    ((1, 2, 6, 5), (0.50, 0.25, 0.75, 0.50)),  # east
    ((3, 7, 6, 2), (0.25, 0.00, 0.50, 0.25)),  # up
    ((4, 0, 1, 5), (0.50, 0.00, 0.75, 0.25)),  # down
)


def rotate_x(point: tuple[float, float, float]):
    x, y, z = point
    return x, -z, y


def rotate_y(point: tuple[float, float, float]):
    x, y, z = point
    return z, y, -x


def rotate_z(point: tuple[float, float, float]):
    x, y, z = point
    return -y, x, z


def transformed(
    point: tuple[int, int, int],
    a: int,
    b: int,
    c: int,
) -> tuple[float, float, float]:
    value = tuple(component * 0.225 for component in point)
    # Legacy OpenGL call order: rotate X, then Y, then Z.
    if c:
        value = rotate_z(value)
    if b:
        value = rotate_y(value)
    if a:
        value = rotate_x(value)
    center = tuple(0.5 + (index * 2 - 1) * 0.25 for index in (a, b, c))
    return tuple(value[index] + center[index] for index in range(3))


def main() -> None:
    lines = [
        "# Reconstructed from TC4 4.2.3.5 TileRunicMatrixRenderer + ModelCube",
        "# Original JAR contains infuser.png but no Runic Matrix OBJ.",
        "mtllib runic_matrix.mtl",
        "usemtl runic_matrix",
    ]
    vertex_offset = 0
    texture_offset = 0
    for a in range(2):
        for b in range(2):
            for c in range(2):
                lines.append(f"o cube_{a}_{b}_{c}")
                for vertex in VERTICES:
                    x, y, z = transformed(vertex, a, b, c)
                    lines.append(f"v {x:.6f} {y:.6f} {z:.6f}")
                for _, (u0, v0, u1, v1) in FACES:
                    # Wavefront V grows from the bottom, while ModelRenderer's
                    # texture rows grow from the top. LegacyObjMesh performs
                    # the standard OBJ V flip when loading, so write the
                    # generated ModelRenderer coordinates in OBJ space here.
                    lines.extend(
                        (
                            f"vt {u0:.6f} {1.0 - v1:.6f}",
                            f"vt {u1:.6f} {1.0 - v1:.6f}",
                            f"vt {u1:.6f} {1.0 - v0:.6f}",
                            f"vt {u0:.6f} {1.0 - v0:.6f}",
                        )
                    )
                for face_index, (indices, _) in enumerate(FACES):
                    vertices = [vertex_offset + index + 1 for index in indices]
                    uv_start = texture_offset + face_index * 4 + 1
                    uvs = [uv_start, uv_start + 1, uv_start + 2, uv_start + 3]
                    refs = " ".join(
                        f"{vertex}/{uv}" for vertex, uv in zip(vertices, uvs)
                    )
                    lines.append(f"f {refs}")
                vertex_offset += len(VERTICES)
                texture_offset += len(FACES) * 4

    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
