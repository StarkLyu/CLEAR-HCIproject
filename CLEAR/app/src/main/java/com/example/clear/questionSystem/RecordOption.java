package com.example.clear.questionSystem;

public class RecordOption extends Option {

    public RecordOption(String text, QuestionSystem system) {
        super(text, OptionType.RECORD, system);
    }

    @Override
    public void onSelected() {
        //Todo: 弹出输入患者信息的表单
        super.onSelected();
    }
}
