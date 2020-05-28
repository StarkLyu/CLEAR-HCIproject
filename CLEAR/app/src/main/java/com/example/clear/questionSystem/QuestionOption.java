package com.example.clear.questionSystem;

import java.util.ArrayList;

public class QuestionOption extends Option {
    public ArrayList<Question> questions;
    public boolean addMode;
    public QuestionOption(String text, ArrayList<Question> questions, boolean add, QuestionSystem system)
    {
        super(text, OptionType.QUESTIONS, system);
        this.questions = questions;
        addMode = add;
    }

    public void onSelected()
    {
        if (!addMode)
        {
            system.deleteRestQuestions();
        }
        system.addQuestionsAfterCurrent(questions);
    }
}
