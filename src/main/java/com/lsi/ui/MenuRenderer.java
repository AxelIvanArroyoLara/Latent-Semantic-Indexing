package com.lsi.ui;

import java.util.List;

public class MenuRenderer {

    public void section(String title) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
    }

    public void lines(List<String> lines) {
        if (lines == null) {
            return;
        }

        for (String line : lines) {
            System.out.println(line == null ? "" : line);
        }
    }
}
