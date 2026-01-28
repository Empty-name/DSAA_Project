public class TwoThreeTree {

    public static final String MIN_INFINITY = ClinicManager.MIN_ID;
    public static final String MAX_INFINITY = ClinicManager.MAX_ID;

    private abstract static class Node {
        Node parent;
        String key;
        int size;

        abstract boolean isLeaf();
    }

    private static class InternalNode extends Node {
        Node left;
        Node middle;
        Node right;

        InternalNode() {
            this.size = 0;
            this.key = MAX_INFINITY;
        }

        @Override
        boolean isLeaf() {
            return false;
        }
    }

    private static class Leaf extends Node {
        Object value;

        Leaf(String key, Object value) {
            this.key = key;
            this.value = value;
            if (key.equals(MIN_INFINITY) || key.equals(MAX_INFINITY)) {
                this.size = 0;
            } else {
                this.size = 1;
            }
        }

        @Override
        boolean isLeaf() {
            return true;
        }
    }

    private InternalNode root;

    public TwoThreeTree() {
        InternalNode x = new InternalNode();
        Leaf l = new Leaf(MIN_INFINITY, null);
        Leaf m = new Leaf(MAX_INFINITY, null);

        l.parent = x;
        m.parent = x;

        x.left = l;
        x.middle = m;
        x.right = null;

        x.key = MAX_INFINITY;
        x.size = 0;

        this.root = x;
    }

    private int compare(String k1, String k2) {
        if (k1.equals(k2)) return 0;
        if (k1.equals(MIN_INFINITY)) return -1;
        if (k2.equals(MIN_INFINITY)) return 1;
        if (k1.equals(MAX_INFINITY)) return 1;
        if (k2.equals(MAX_INFINITY)) return -1;
        return k1.compareTo(k2);
    }

    private void updateKey(InternalNode x) {
        if (x == null) return;
        if (x.right != null) {
            x.key = x.right.key;
        } else if (x.middle != null) {
            x.key = x.middle.key;
        } else {
            x.key = x.left.key;
        }
    }

    private void updateSize(InternalNode x) {
        if (x == null) return;
        int s = 0;
        if (x.left != null) s += x.left.size;
        if (x.middle != null) s += x.middle.size;
        if (x.right != null) s += x.right.size;
        x.size = s;
    }

    private void setChildren(InternalNode x, Node l, Node m, Node r) {
        x.left = l;
        x.middle = m;
        x.right = r;

        if (l != null) l.parent = x;
        if (m != null) m.parent = x;
        if (r != null) r.parent = x;

        updateKey(x);
        updateSize(x);
    }

    public Object search(String k) {
        return searchRec(root, k);
    }

    private Object searchRec(Node x, String k) {
        if (x.isLeaf()) {
            if (x.key.equals(k)) {
                return ((Leaf) x).value;
            } else {
                return null;
            }
        }

        InternalNode node = (InternalNode) x;
        if (compare(k, node.left.key) <= 0) {
            return searchRec(node.left, k);
        } else if (compare(k, node.middle.key) <= 0) {
            return searchRec(node.middle, k);
        } else {
            return searchRec(node.right, k);
        }
    }
    private void updateStatsUpwards(InternalNode node) {
        while (node != null) {
            updateKey(node);
            updateSize(node);
            node = (InternalNode) node.parent;
        }
    }

    public void insert(String key, Object value) {
        Leaf z = new Leaf(key, value);

        Node y = root;
        while (!y.isLeaf()) {
            InternalNode inner = (InternalNode) y;

            if (compare(key, inner.left.key) < 0) y = inner.left;
            else if (compare(key, inner.middle.key) < 0) y = inner.middle;
            else y = inner.right;
        }

        InternalNode x = (InternalNode) y.parent;

        Node newSibling = insertAndSplit(x, z);

        while (x != root && newSibling != null) {
            x = (InternalNode) x.parent;
            newSibling = insertAndSplit(x, newSibling);
        }

        if (newSibling != null) {
            InternalNode w = new InternalNode();
            setChildren(w, root, newSibling, null);
            root = w;
        } else {
            updateStatsUpwards(x);
        }
    }

    private Node insertAndSplit(InternalNode x, Node z) {
        Node l = x.left;
        Node m = x.middle;
        Node r = x.right;

        if (r == null) {
            if (compare(z.key, l.key) < 0) {
                setChildren(x, z, l, m);
            } else if (compare(z.key, m.key) < 0) {
                setChildren(x, l, z, m);
            } else {
                setChildren(x, l, m, z);
            }
            return null;
        }

        InternalNode y = new InternalNode();

        if (compare(z.key, l.key) < 0) {
            setChildren(x, z, l, null);
            setChildren(y, m, r, null);
        } else if (compare(z.key, m.key) < 0) {
            setChildren(x, l, z, null);
            setChildren(y, m, r, null);
        } else if (compare(z.key, r.key) < 0) {
            setChildren(x, l, m, null);
            setChildren(y, z, r, null);
        } else {
            setChildren(x, l, m, null);
            setChildren(y, r, z, null);
        }

        return y;
    }

    public void delete(String key) {
        Node node = findNode(root, key);
        if (node == null || !(node instanceof Leaf)) return;

        Leaf x = (Leaf) node;
        InternalNode y = (InternalNode) x.parent;

        if (x == y.left) {
            setChildren(y, y.middle, y.right, null);
        } else if (x == y.middle) {
            setChildren(y, y.left, y.right, null);
        } else {
            setChildren(y, y.left, y.middle, null);
        }

        while (y != null) {
            updateKey(y);
            updateSize(y);

            if (y != root && (y.middle == null)) {
                y = borrowOrMerge(y);
            } else {

                if (y == root && y.middle == null && !y.left.isLeaf()) {
                    root = (InternalNode) y.left;
                    root.parent = null;
                }
                y = (InternalNode) y.parent;
            }
        }
    }

    private Node findNode(Node x, String k) {
        if (x.isLeaf()) return x.key.equals(k) ? x : null;
        InternalNode n = (InternalNode) x;
        if (compare(k, n.left.key) <= 0) return findNode(n.left, k);
        else if (compare(k, n.middle.key) <= 0) return findNode(n.middle, k);
        else return findNode(n.right, k);
    }


    private InternalNode borrowOrMerge(InternalNode y) {
        InternalNode z = (InternalNode) y.parent;
        if (z == null) return null;

        InternalNode x;

        if (y == z.left) {
            x = (InternalNode) z.middle;

            if (x.right != null) {
                setChildren(y, y.left, x.left, null);
                setChildren(x, x.middle, x.right, null);
            }

            else {
                setChildren(x, y.left, x.left, x.middle);
                setChildren(z, x, z.right, null);
            }
        }
        else if (y == z.middle) {
            x = (InternalNode) z.left;
            if (x.right != null) {
                setChildren(y, x.right, y.left, null);
                setChildren(x, x.left, x.middle, null);
            } else {
                setChildren(x, x.left, x.middle, y.left);
                setChildren(z, x, z.right, null);
            }
        }
        else {
            x = (InternalNode) z.middle;
            if (x.right != null) {
                setChildren(y, x.right, y.left, null);
                setChildren(x, x.left, x.middle, null);
            } else {
                setChildren(x, x.left, x.middle, y.left);
                setChildren(z, z.left, x, null);
            }
        }

        if (y != null) {
            updateKey(y);
            updateSize(y);
        }
        return z;
    }
}

