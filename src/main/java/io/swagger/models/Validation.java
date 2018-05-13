package io.swagger.models;

import java.util.List;

public class Validation {
        private boolean valid;
        private List<String> messages;

        public Validation valid(boolean valid) {
            this.valid = valid;
            return this;
        }


        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }


        public List<String> getMessages() {
            return messages;
        }

        public void setMessages(List<String> messages) {
            this.messages = messages;
        }

}
