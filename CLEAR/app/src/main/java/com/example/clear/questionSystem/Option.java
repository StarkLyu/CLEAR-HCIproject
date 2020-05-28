package com.example.clear.questionSystem;

public class Option {
    public String text;
    public OptionType type;
    public QuestionSystem system;
    public Option(String text, OptionType type, QuestionSystem system)
    {
        this.text = text;
        this.type = type;
        this.system = system;
    }

    public void onSelected()
    {
        System.out.println(type);
    }
}
