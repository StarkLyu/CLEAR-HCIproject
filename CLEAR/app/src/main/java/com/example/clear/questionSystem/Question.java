package com.example.clear.questionSystem;

import java.lang.reflect.Array;
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

    // 根据序号选择一个option，并执行该option绑定的操作，即onSelected
    public void select(int index) throws Exception {
        if (index < 0 || index >= options.size()) throw new Exception("Invalid selected option");
        options.get(index).onSelected();
    }

    // 获取option的显示文字列表
    public ArrayList<String> optionTexts() {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < names.size(); i++)
        {
            names.add(options.get(i).text);
        }
        return names;
    }
}
