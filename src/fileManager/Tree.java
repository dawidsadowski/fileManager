package fileManager;

import java.util.ArrayList;
import java.util.List;

public class Tree<T> {
    private List<Tree<T>> children = new ArrayList<>();
    private Tree<T> parent = null;
    private T data;

    public Tree(T data) {
        this.data = data;
    }

    public Tree(T data, Tree<T> parent) {
        this.data = data;
        this.parent = parent;
    }

    public List<Tree<T>> getChildren() {
        return children;
    }

    public Tree<T> getRoot() {
        Tree<T> current = this;

        while((current != null ? current.getParent() : null) == null) {
            current = null;
        }

        return current;
    }

    // Only for String
    public String getRootPath() {
        String path = "";

        Tree<T> parent = getParent();

        if(parent == null) {
            return path;
        }

        while(parent.getParent() != null) {
            path = parent.getData() + "/" + path;
            parent = parent.getParent();
        }

        return path;
    }

    public Tree<T> getParent() {
        return this.parent;
    }

    public void setParent(Tree<T> parent) {
//        parent.addChild(this);
        this.parent = parent;
    }

    public Tree<T> addChild(T data) {
        Tree<T> child = new Tree<>(data);
        child.setParent(this);
        this.children.add(child);

        return this.children.get(this.children.size() - 1);
    }

    public void addChild(Tree<T> child) {
        child.setParent(this);
        this.children.add(child);
    }

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean isRoot() {
        return (this.parent == null);
    }

    public boolean isLeaf() {
        return this.children.size() == 0;
    }

    public void removeParent() {
        this.parent = null;
    }

    public void print(Tree<T> root) {
        if(root.getParent() != null) {
            System.out.print(root.data + " ");
        }

        for(Tree<T> node : root.children) {
            print(node);
        }
    }
}