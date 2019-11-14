package utils;

import java.io.Serializable;

public class Pair<T, U> implements Serializable {

    private static final long serialVersionUID = 1L;
    private T left;
    private U right;
    
    public Pair(T left, U right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Pair) {
            Pair p = (Pair) o;

            return p.getKey().equals(this.left) && p.getValue().equals(this.right);
        }
        else
            return false;
    }

    public T getKey() {
        return left;
    }

    public U getValue() {
        return right;
    }

    public void setKey(T newKey) {
        left = newKey;
    }

    public void setValue(U newValue) {
        right = newValue;
    }
}