package com.project.lawrence.insurance_tracker.dto;


import java.util.List;

public class ChatResponse {

    private List<Choice> choices;

    public static class Choice{
        private int index;
        private Message Message;

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Message getMessage() {
            return Message;
        }

        public void setMessage(Message Message) {
            this.Message = Message;
        }
    }

    public List<Choice> getChoices() {
        return choices;
    }

    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
}
