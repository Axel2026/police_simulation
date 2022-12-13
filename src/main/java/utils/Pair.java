package utils;

public class Pair<T, K> {

    private T item1;
    private K item2;

    public Pair(T item1, K item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    public T getItem1() {
        return item1;
    }

    public K getItem2() {
        return item2;
    }
}
