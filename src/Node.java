public class Node {
    private Patient val;
    private int key;
    private Node parent;
    private Node left;
    private Node middle;
    private Node right;

    public Node(Patient val) {
        this.val = val;
    }

    public Node() {}

    public Patient getVal() {
        return this.val;
    }

    public void setVal(Patient val) {
        this.val = val;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Node getParent() {
        return this.parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node getLeft() {
        return this.left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getMiddle() {
        return this.middle;
    }

    public void setMiddle(Node middle) {
        this.middle = middle;
    }

    public Node getRight() {
        return this.right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public boolean isLeaf() {
        return this.left == null && this.middle == null && this.right == null;
    }
}