package com.example.clear.questionSystem;

import java.util.ArrayList;

public class Question {
    public String text;
    public ArrayList<Option> options;
    public Question()
    {
        options = new ArrayList<>();
    }

    public Question(String t)
    {
        text = t;
        options = new ArrayList<>();
    }

    public void select(int index) throws Exception {
        if (index < 0 || index >= options.size()) throw new Exception("Invalid selected option");
        options.get(index).onSelected();
    }
}
