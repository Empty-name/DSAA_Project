public class PatientsQueue {
    private final int MIN_VALUE = Integer.MIN_VALUE;
    private final int MAX_VALUE = Integer.MAX_VALUE;
    private Node root;
    //Node for minimum value caching
    private Node minNode;
    private int size;
    private int ticketCounter;


    public PatientsQueue() {
        Node x = new Node();
        Node l = new Node();
        Node m = new Node();
        l.setKey(MIN_VALUE);
        m.setKey(MAX_VALUE);
        l.setParent(x);
        m.setParent(x);
        x.setKey(MAX_VALUE);
        x.setLeft(l);
        x.setMiddle(m);

        this.root = x;
        this.size = 0;
        this.ticketCounter = 0;
        this.minNode = null;
    }

    public int getSize() {
        return this.size;
    }

    public Node getMinimum() {
        return this.minNode;
    }

    public Node search(Node x, int key) {
        if (x.isLeaf()) {
            if(x.getKey() == key)
                return x;
            else
                return null;
        }
        if (key <= x.getLeft().getKey()) {
            return search(x.getLeft(), key);
        } else {
            if(key <= x.getMiddle().getKey())
                return search(x.getMiddle(), key);
            else
                return search(x.getRight(), key);
        }
    }

    private Node findMinTraversal() {
        if (this.root == null) return null;

        Node x = this.root;

        while (!x.isLeaf()) {
            x = x.getLeft();
        }
        if (x.getKey() == MIN_VALUE) {
            Node parent = x.getParent();
            if (parent != null) {
                Node next = parent.getMiddle();
                if (next != null) {
                    while (!next.isLeaf()) {
                        next = next.getLeft();
                    }
                    x = next;
                }
            }
        }
        if (x.getKey() == MAX_VALUE) return null;

        return x;
    }

    public void updateKeys(Node x) {
        x.setKey(x.getLeft().getKey());
        if (x.getMiddle() != null)
            x.setKey(x.getMiddle().getKey());
        if (x.getRight() != null)
            x.setKey(x.getRight().getKey());
    }

    public void setChildren(Node x, Node left, Node middle, Node right) {
        x.setLeft(left);
        x.setMiddle(middle);
        x.setRight(right);
        left.setParent(x);
        if (middle != null)
            middle.setParent(x);
        if (right != null)
            right.setParent(x);
        updateKeys(x);
    }

    public Node insertAndSplit(Node x, Node z) {
        Node l = x.getLeft();
        Node m = x.getMiddle();
        Node r = x.getRight();
        if (r == null) {
            if (z.getKey() < l.getKey())
                setChildren(x, z, l, m);
            else if(z.getKey() < m.getKey())
                setChildren(x, l, z, m);
            else
                setChildren(x, l, m, z);
            return null;
        }
        Node y = new Node();
        if (z.getKey() < l.getKey()) {
            setChildren(x, z, l, null);
            setChildren(y, m, r, null);
        } else if (z.getKey() < x.getMiddle().getKey()) {
            setChildren(x, l, z, null);
            setChildren(y, m, r, null);
        } else if (z.getKey() < r.getKey()) {
            setChildren(x, l, m, null);
            setChildren(y, z, r, null);
        } else {
            setChildren(x, l, m, null);
            setChildren(y, r, z, null);
        }
        return y;
    }

    public void insert(Patient p) {
        int newTicket = this.ticketCounter++;
        p.setTicketNumber(newTicket);

        Node z = new Node(p);
        z.setKey(newTicket);

        if (this.minNode == null) {
            this.minNode = z;
        }

        this.size++;
        Node y = this.root;
        while (!(y.isLeaf())) {
            if (z.getKey() < y.getLeft().getKey()) {
                y = y.getLeft();
            } else if (y.getMiddle() != null && z.getKey() < y.getMiddle().getKey()) {
                y = y.getMiddle();
            } else {
                y = y.getRight();
            }
        }
        Node x = y.getParent();
        z = insertAndSplit(x, z);
        while (x != this.root) {
            x = x.getParent();
            if (z != null) {
                z = insertAndSplit(x, z);
            } else {
                updateKeys(x);
            }
        }
        if (z != null) {
            Node w = new Node();
            setChildren(w, x, z, null);
            this.root = w;
        }
    }

    public Node borrowOrMerge(Node y) {
        Node z = y.getParent();
        if (y == z.getLeft()) {
            Node x = z.getMiddle();
            if (x.getRight() != null) {
                setChildren(y, y.getLeft(), x.getLeft(), null);
                setChildren(x, x.getMiddle(), x.getRight(), null);
            } else {
                setChildren(x, y.getLeft(), x.getLeft(), x.getMiddle());
                setChildren(z, x, z.getRight(), null);
            }
            return z;
        }
        if (y == z.getMiddle()) {
            Node x = z.getLeft();
            if (x.getRight() != null) {
                setChildren(y, x.getRight(), y.getLeft(), null);
                setChildren(x, x.getLeft(), x.getMiddle(), null);
            } else {
                setChildren(x, x.getLeft(), x.getMiddle(), y.getLeft());
                setChildren(z, x, z.getRight(), null);
            }
            return z;
        }
        if (y == z.getRight()) {
            Node x = z.getMiddle();
            if (x.getRight() != null) {
                setChildren(y, x.getRight(), y.getLeft(), null);
                setChildren(x, x.getLeft(), x.getMiddle(), null);
            } else {
                setChildren(x, x.getLeft(), x.getMiddle(), y.getLeft());
                setChildren(z, z.getLeft(), x, null);
            }
        }
        return z;
    }

    public void delete(Node x) {
        if (x == null) return;

        boolean isRemovingMin = (x == this.minNode); //Flag to check if we are removing the minimum node

        if (x.isLeaf()) {
            this.size--;
        }
        Node y = x.getParent();
        if (x == y.getLeft()) {
            setChildren(y, y.getMiddle(), y.getRight(), null);
        }
        else if (x == y.getMiddle()) {
            setChildren(y, y.getLeft(), y.getRight(), null);
        }
        else
            setChildren(y, y.getLeft(), y.getMiddle(), null);
        while (y != null) {
            if (y.getMiddle() != null) {
                updateKeys(y);
                y = y.getParent();
            }
            else {
                if (y != this.root) {
                    y = borrowOrMerge(y);
                }
                else {
                    this.root = y.getLeft();
                    y.getLeft().setParent(null);
                    break;
                }
            }
        }

        if (isRemovingMin) {
            if (this.size == 0) {
                this.minNode = null;
            } else {
                //O(log P) required to find the new minimum node.
                //This is acceptable because happens only inside delete method or nextPatientLeave.
                //Both cases have acceptable complexity O(log D + log P).
                this.minNode = findMinTraversal();
            }
        }
    }

    public Node searchKey(int key) {
        return search(this.root, key);
    }
}
