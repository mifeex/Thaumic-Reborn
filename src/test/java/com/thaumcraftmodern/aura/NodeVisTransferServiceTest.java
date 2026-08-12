package com.thaumcraftmodern.aura;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NodeVisTransferServiceTest {
    @Test
    void transferFillsOnlyMissingCapacityAndDrainsExactlyThatAmount() {
        AuraNodeState node = AuraNodeFactory.ordinary(UUID.randomUUID());
        FakeWand wand = new FakeWand(20, 25);
        NodeVisTransferService service = new NodeVisTransferService(
                new OperationNonceGuard()
        );
        NodeVisTransferService.Request request = validRequest(node, UUID.randomUUID());

        NodeVisTransferService.Result result = service.transfer(request, node, wand);

        assertEquals(NodeVisTransferService.Status.TRANSFERRED, result.status());
        for (PrimalAspect aspect : PrimalAspect.ordered()) {
            assertEquals(5, result.moved().get(aspect));
            assertEquals(95, node.current(aspect));
            assertEquals(25, wand.snapshot().current().get(aspect));
        }
    }

    @Test
    void remoteClientAndStaleNodeCannotMutateEitherStore() {
        AuraNodeState node = AuraNodeFactory.ordinary(UUID.randomUUID());
        FakeWand wand = new FakeWand(0, 25);
        NodeVisTransferService service = new NodeVisTransferService(
                new OperationNonceGuard()
        );

        NodeVisTransferService.Request client = new NodeVisTransferService.Request(
                UUID.randomUUID(),
                UUID.randomUUID(),
                node.nodeId(),
                false,
                true,
                true,
                1.0D,
                6.0D
        );
        assertEquals(
                NodeVisTransferService.Status.NOT_SERVER,
                service.transfer(client, node, wand).status()
        );
        assertEquals(100, node.current(PrimalAspect.AER));
        assertEquals(0, wand.snapshot().current().get(PrimalAspect.AER));

        NodeVisTransferService.Request changed = new NodeVisTransferService.Request(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                true,
                true,
                1.0D,
                6.0D
        );
        assertEquals(
                NodeVisTransferService.Status.NODE_CHANGED,
                service.transfer(changed, node, wand).status()
        );
    }

    @Test
    void replayedOperationCannotDrainTwice() {
        AuraNodeState node = AuraNodeFactory.ordinary(UUID.randomUUID());
        FakeWand wand = new FakeWand(0, 25);
        NodeVisTransferService service = new NodeVisTransferService(
                new OperationNonceGuard()
        );
        UUID operation = UUID.randomUUID();
        NodeVisTransferService.Request request = validRequest(node, operation);

        assertEquals(
                NodeVisTransferService.Status.TRANSFERRED,
                service.transfer(request, node, wand).status()
        );
        assertEquals(
                NodeVisTransferService.Status.DUPLICATE_OPERATION,
                service.transfer(request, node, wand).status()
        );
        assertEquals(75, node.current(PrimalAspect.AER));
        assertEquals(25, wand.snapshot().current().get(PrimalAspect.AER));
    }

    @Test
    void classicBoundedTransferMovesOnlySelectedAspectAndCanPreserveLastVis() {
        AuraNodeState node = new AuraNodeState(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                Map.of(
                        PrimalAspect.AER, 1,
                        PrimalAspect.TERRA, 10,
                        PrimalAspect.IGNIS, 10,
                        PrimalAspect.AQUA, 10,
                        PrimalAspect.ORDO, 10,
                        PrimalAspect.PERDITIO, 10
                ),
                PrimalVis.uniform(10),
                0L
        );
        FakeWand wand = new FakeWand(0, 25);
        NodeVisTransferService service = new NodeVisTransferService(
                new OperationNonceGuard()
        );

        NodeVisTransferService.Result preserved = service.transferAspect(
                validRequest(node, UUID.randomUUID()),
                node,
                wand,
                PrimalAspect.AER,
                2,
                true
        );
        assertEquals(
                NodeVisTransferService.Status.NOTHING_TO_TRANSFER,
                preserved.status()
        );
        assertEquals(1, node.current(PrimalAspect.AER));

        NodeVisTransferService.Result moved = service.transferAspect(
                validRequest(node, UUID.randomUUID()),
                node,
                wand,
                PrimalAspect.TERRA,
                2,
                false
        );
        assertEquals(NodeVisTransferService.Status.TRANSFERRED, moved.status());
        assertEquals(2, moved.moved().get(PrimalAspect.TERRA));
        assertEquals(0, moved.moved().get(PrimalAspect.AER));
        assertEquals(8, node.current(PrimalAspect.TERRA));
        assertEquals(2, wand.snapshot().current().get(PrimalAspect.TERRA));
    }

    @Test
    void classicTapFillsTheLastFractionalCentivisLikeOriginalAddVis() {
        AuraNodeState node = AuraNodeFactory.ordinary(UUID.randomUUID());
        FakeWand wand = new FakeWand(4_995, 5_000, 100);
        NodeVisTransferService service = new NodeVisTransferService(
                new OperationNonceGuard()
        );

        NodeVisTransferService.Result result = service.transferAspect(
                validRequest(node, UUID.randomUUID()),
                node,
                wand,
                PrimalAspect.AER,
                1,
                false
        );

        assertEquals(NodeVisTransferService.Status.TRANSFERRED, result.status());
        assertEquals(1, result.moved().get(PrimalAspect.AER));
        assertEquals(99, node.current(PrimalAspect.AER));
        assertEquals(5_000, wand.snapshot().current().get(PrimalAspect.AER));
    }

    @Test
    void sparseClassicNodeTransfersPrimalWithoutAddingMissingAspectKeys() {
        AuraNodeState node = AuraNodeState.withAspects(
                UUID.randomUUID(),
                AuraNodeType.NORMAL,
                AuraNodeModifier.NORMAL,
                Map.of(
                        "aer", 8,
                        "motus", 12
                ),
                Map.of(
                        "aer", 8,
                        "motus", 12
                ),
                0L
        );
        FakeWand wand = new FakeWand(0, 25);
        NodeVisTransferService service = new NodeVisTransferService(
                new OperationNonceGuard()
        );

        NodeVisTransferService.Result result = service.transferAspect(
                validRequest(node, UUID.randomUUID()),
                node,
                wand,
                PrimalAspect.AER,
                2,
                false
        );

        assertEquals(NodeVisTransferService.Status.TRANSFERRED, result.status());
        assertEquals(6, node.current(PrimalAspect.AER));
        assertEquals(2, wand.snapshot().current().get(PrimalAspect.AER));
        assertEquals(
                Map.of("aer", 6, "motus", 12),
                node.snapshot().aspectsCurrent()
        );
        assertEquals(
                Map.of("aer", 8, "motus", 12),
                node.snapshot().aspectsMaximum()
        );
    }

    private static NodeVisTransferService.Request validRequest(
            AuraNodeState node,
            UUID operation
    ) {
        return new NodeVisTransferService.Request(
                UUID.randomUUID(),
                operation,
                node.nodeId(),
                true,
                true,
                true,
                2.0D,
                6.0D
        );
    }

    private static final class FakeWand implements WandVisStore {
        private final EnumMap<PrimalAspect, Integer> current;
        private final Map<PrimalAspect, Integer> capacity;
        private final int unitsPerNodeVis;
        private long revision;

        private FakeWand(int current, int capacity) {
            this(current, capacity, 1);
        }

        private FakeWand(int current, int capacity, int unitsPerNodeVis) {
            this.current = new EnumMap<>(PrimalVis.uniform(current));
            this.capacity = PrimalVis.uniform(capacity);
            this.unitsPerNodeVis = unitsPerNodeVis;
        }

        @Override
        public int unitsPerNodeVis() {
            return unitsPerNodeVis;
        }

        @Override
        public Snapshot snapshot() {
            return new Snapshot(current, capacity, revision);
        }

        @Override
        public boolean replaceCurrent(
                long expectedRevision,
                Map<PrimalAspect, Integer> nextCurrent
        ) {
            if (revision != expectedRevision) {
                return false;
            }
            current.clear();
            current.putAll(PrimalVis.exact(nextCurrent, "next"));
            revision++;
            return true;
        }

        @Override
        public boolean restore(Snapshot snapshot, long expectedRevision) {
            if (revision != expectedRevision) {
                return false;
            }
            current.clear();
            current.putAll(snapshot.current());
            revision = snapshot.revision();
            return true;
        }
    }
}
