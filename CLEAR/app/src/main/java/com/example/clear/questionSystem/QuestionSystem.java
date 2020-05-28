package com.example.clear.questionSystem;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ArrayList;

public class QuestionSystem {
    public ArrayList<Question> questionList;
    public Iterator<Question> iterator;
    public int currentQuestion;

    public QuestionSystem(String data)
    {
        try{
            JSONArray jsonArray = JSON.parseArray(data);
            this.questionList = parseQuestions(jsonArray);
            iterator = this.questionList.iterator();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private ArrayList<Question> parseQuestions(JSONArray jsonArray)
    {
        ArrayList<Question> res = new ArrayList<Question>();
        try {
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject quesObj = jsonArray.getJSONObject(i);
                Question q = new Question();
                q.text = quesObj.getString("question");
                JSONArray options = quesObj.getJSONArray("options");
                for (int j = 0; j < options.size(); j++)
                {
                    JSONObject optionObj = options.getJSONObject(j);
                    Option addOption;
                    String optionText = optionObj.getString("text");
                    switch (optionObj.getString("type"))
                    {
                        case "finish":
                            addOption = new Option(optionText, OptionType.NEXT, this);
                            break;
                        case "record":
                            addOption = new RecordOption(optionText,this);
                            break;
                        case "questions":
                            addOption = new QuestionOption(optionText, parseQuestions(optionObj.getJSONArray("questions")), true, this);
                            break;
                        case "switchQuestions":
                            addOption = new QuestionOption(optionText, parseQuestions(optionObj.getJSONArray("questions")), false, this);
                            break;
                        default:
                            throw new Exception("Invalid Option");
                    }
                    q.options.add(addOption);
                }
                res.add(q);
            }
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return res;
    }

    //获取当前问题
    public Question getCurrentQuestion()
    {
        return questionList.get(currentQuestion);
    }

    //切换到下一个问题，切换成功返回true，失败返回false
    public boolean nextQuestion()
    {
        if (hasNext())
        {
            currentQuestion++;
            return true;
        }
        else return false;
    }

    //判断是否有下一个问题
    public boolean hasNext()
    {
        return currentQuestion < questionList.size() - 1;
    }

    public void addQuestionsAfterCurrent(ArrayList<Question> adders)
    {
        questionList.addAll(currentQuestion + 1, adders);
    }

    public void deleteRestQuestions()
    {
        questionList.subList(currentQuestion + 1, questionList.size()).clear();
    }
}
