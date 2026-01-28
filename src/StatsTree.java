public class StatsTree {
    private final int MIN_VALUE = Integer.MIN_VALUE;
    private final int MAX_VALUE = Integer.MAX_VALUE;
    private Node root;

    private static class Node {
        int key;
        Node parent, left, middle, right;

        int count;

        int subTreeDoctors;
        int subTreePatients;

        Node() {
            this.count = 0;
            this.subTreeDoctors = 0;
            this.subTreePatients = 0;
        }

        Node(int key) {
            this.key = key;
            this.count = 1;
            this.subTreeDoctors = 1;
            this.subTreePatients = key;
        }

        public boolean isLeaf() {
            return left == null && middle == null && right == null;
        }
    }

    public StatsTree() {
        Node x = new Node();
        Node l = new Node();
        Node m = new Node();

        l.key = MIN_VALUE;
        m.key = MAX_VALUE;

        l.parent = x;
        m.parent = x;
        x.key = MAX_VALUE;
        x.left = l;
        x.middle = m;

        this.root = x;
    }

    private void updateStats(Node x) {
        if (x == null) return;
        if (x.left != null) {
            x.key = x.left.key;
            if (x.middle != null) x.key = x.middle.key;
            if (x.right != null) x.key = x.right.key;
        }

        if (x.isLeaf()) {
            x.subTreeDoctors = x.count;
            x.subTreePatients = x.key * x.count;
        } else {
            int docSum = 0;
            int patSum = 0;

            if (x.left != null) {
                docSum += x.left.subTreeDoctors;
                patSum += x.left.subTreePatients;
            }
            if (x.middle != null) {
                docSum += x.middle.subTreeDoctors;
                patSum += x.middle.subTreePatients;
            }
            if (x.right != null) {
                docSum += x.right.subTreeDoctors;
                patSum += x.right.subTreePatients;
            }

            x.subTreeDoctors = docSum;
            x.subTreePatients = patSum;
        }
    }

    private void updatePathToRoot(Node x) {
        while (x != null) {
            updateStats(x);
            x = x.parent;
        }
    }

    public void increaseCount(int load) {
        Node leaf = search(root, load);

        if (leaf != null) {
            leaf.count++;
            updatePathToRoot(leaf);
        }
        else {
            insertNewLoad(load);
        }
    }

    public void decreaseCount(int load) {
        Node leaf = search(root, load);
        if (leaf != null) {
            leaf.count--;
            if (leaf.count == 0) {
                deleteNode(leaf);
            }
            else {
                updatePathToRoot(leaf);
            }
        }
    }

    private Node search(Node x, int key) {
        if (x.isLeaf()) {
            return (x.key == key) ? x : null;
        }
        if (key <= x.left.key) {
            return search(x.left, key);
        }
        else {
            if (key <= x.middle.key)
                return search(x.middle, key);
            else
                return search(x.right, key);
        }
    }

    private void setChildren(Node x, Node left, Node middle, Node right) {
        x.left = left;
        x.middle = middle;
        x.right = right;

        if (left != null) left.parent = x;
        if (middle != null) middle.parent = x;
        if (right != null) right.parent = x;

        updateStats(x);
    }

    private void insertNewLoad(int load) {
        Node z = new Node(load);

        Node y = this.root;
        while (!y.isLeaf()) {
            if (z.key < y.left.key) {
                y = y.left;
            }
            else {
                if (y.middle != null && z.key < y.middle.key) {
                    y = y.middle;
                }
                else {
                    y = y.right;
                }
            }
        }

        Node x = y.parent;
        Node splitNode = insertAndSplit(x, z);

        while (x != this.root) {
            x = x.parent;
            if (splitNode != null) {
                splitNode = insertAndSplit(x, splitNode);
            } else {
                updateStats(x);
            }
        }

        if (splitNode != null) {
            Node w = new Node();
            setChildren(w, x, splitNode, null);
            this.root = w;
        }
    }

    private Node insertAndSplit(Node x, Node z) {
        Node l = x.left;
        Node m = x.middle;
        Node r = x.right;

        if (r == null) {
            if (z.key < l.key) {
                setChildren(x, z, l, m);
            }
            else {
                if (z.key < m.key)
                    setChildren(x, l, z, m);
                else
                    setChildren(x, l, m, z);
            }
            return null;
        }

        Node y = new Node();
        if (z.key < l.key) {
            setChildren(x, z, l, null);
            setChildren(y, m, r, null);
        } else if (z.key < m.key) {
            setChildren(x, l, z, null);
            setChildren(y, m, r, null);
        } else if (z.key < r.key) {
            setChildren(x, l, m, null);
            setChildren(y, z, r, null);
        } else {
            setChildren(x, l, m, null);
            setChildren(y, r, z, null);
        }
        return y;
    }

    private void deleteNode(Node x) {
        if (x == null) return;

        Node y = x.parent;
        if (y == null) {
            return;
        }

        if (x == y.left) setChildren(y, y.middle, y.right, null);
        else if (x == y.middle) setChildren(y, y.left, y.right, null);
        else setChildren(y, y.left, y.middle, null);

        while (y != null) {
            if (y.middle != null) {
                updateStats(y);
                y = y.parent;
            } else {
                if (y != this.root) {
                    y = borrowOrMerge(y);
                } else {
                    this.root = y.left;
                    y.left.parent = null;
                    return;
                }
            }
        }
    }

    private Node borrowOrMerge(Node y) {
        Node z = y.parent;
        if (y == z.left) {
            Node x = z.middle;
            if (x.right != null) {
                setChildren(y, y.left, x.left, null);
                setChildren(x, x.middle, x.right, null);
            } else {
                setChildren(x, y.left, x.left, x.middle);
                setChildren(z, x, z.right, null);
            }
        } else if (y == z.middle) {
            Node x = z.left;
            if (x.right != null) {
                setChildren(y, x.right, y.left, null);
                setChildren(x, x.left, x.middle, null);
            } else {
                setChildren(x, x.left, x.middle, y.left);
                setChildren(z, x, z.right, null);
            }
        } else {
            Node x = z.middle;
            if (x.right != null) {
                setChildren(y, x.right, y.left, null);
                setChildren(x, x.left, x.middle, null);
            } else {
                setChildren(x, x.left, x.middle, y.left);
                setChildren(z, z.left, x, null);
            }
        }
        return z;
    }

    public int getCountDoctorsLeq(int k) {
        return getCountLeqRec(root, k);
    }

    private int getCountLeqRec(Node x, int k) {
        if (x.isLeaf()) {
            if (x.key <= k)
                return x.subTreeDoctors;
            else
                return 0;
        }
        if (k <= x.left.key) {
            return getCountLeqRec(x.left, k);
        }
        else {
            if (k <= x.middle.key)
                return x.left.subTreeDoctors + getCountLeqRec(x.middle, k);
            else
                return x.left.subTreeDoctors + x.middle.subTreeDoctors + getCountLeqRec(x.right, k);
        }
    }

    public int getSumPatientsLeq(int k) {
        return getSumPatientsLeqRec(root, k);
    }

    private int getSumPatientsLeqRec(Node x, int k) {
        if (x.isLeaf()) {
            if (x.key <= k)
                return x.subTreePatients;
            else return 0;
        }
        if (k <= x.left.key) {
            return getSumPatientsLeqRec(x.left, k);
        }
        else {
            if (k <= x.middle.key)
                return x.left.subTreePatients + getSumPatientsLeqRec(x.middle, k);
            else
                return x.left.subTreePatients + x.middle.subTreePatients + getSumPatientsLeqRec(x.right, k);
        }
    }
}