package com.deemo.collection;

import java.util.ArrayList;
import java.util.List;

public class DeemoList {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("aaa");
        list.add("bbb");
        list.add("ccc");
        list.add("ddd");

        ArrayList<String> copyList = new ArrayList<>(list);
        System.out.println(copyList);

        list.set(0, "AAA");
        System.out.println(list);
        System.out.println(copyList);

        copyList.set(0, "AaA");
        System.out.println(list);
        System.out.println(copyList);

        DeemoList deemoList = new DeemoList();
        Person person = new Person();
        person.name = "aaa";
        deemoList.change(person);
        System.out.println(person.name);
    }

    private void change(Person person) {
        person = new Person();
    }

}

class Person {
    String name;
}
