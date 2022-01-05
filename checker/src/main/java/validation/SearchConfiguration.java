package validation;

import history.VisibilityType;

public class SearchConfiguration implements Cloneable {
    private int searchMode = 0; // 0: dfs, 1: bfs, 2: h*
    private int searchLimit = -1;
    private int queueLimit = -1;
    private int visibilityLimit = -1; // -1: unlimited, 0:size of lin
    private boolean findAllAbstractExecution = false;
    private boolean enablePrickOperation = true;
    private boolean enableOutputSchedule = true;
    private boolean enableIncompatibleRelation = true;
    private VisibilityType visibilityType = VisibilityType.CAUSAL;
    private String adt = null;

//    public SearchConfiguration() {
//        this.searchMode = 0;
//        this.searchLimit = -1;
//        this.queueLimit = -1;
//    }
    
    public SearchConfiguration(Builder builder) {
        this.searchMode = builder.searchMode;
        this.searchLimit = builder.searchLimit;
        this.queueLimit = builder.queueLimit;
        this.visibilityLimit = builder.visibilityLimit;
        this.findAllAbstractExecution = builder.findAllAbstractExecution;
        this.enablePrickOperation = builder.enablePrickOperation;
        this.enableOutputSchedule = builder.enableOutputSchedule;
        this.enableIncompatibleRelation = builder.enableIncompatibleRelation;
        this.visibilityType = builder.visibilityType;
        this.adt = builder.adt;
    }

    public int getSearchMode() {
        return searchMode;
    }

    public int getSearchLimit() {
        return searchLimit;
    }

    public int getQueueLimit() {
        return queueLimit;
    }

    public int getVisibilityLimit() {
        return visibilityLimit;
    }

    public void setAdt(String adt) {
        this.adt = adt;
    }

    public String getAdt() {
        return adt;
    }

    public boolean isFindAllAbstractExecution() {
        return findAllAbstractExecution;
    }

    public void setFindAllAbstractExecution(boolean findAllAbstractExecution) {
        this.findAllAbstractExecution = findAllAbstractExecution;
    }

    public boolean isEnablePrickOperation() {
        return enablePrickOperation;
    }

    public void setEnablePrickOperation(boolean enablePrickOperation) {
        this.enablePrickOperation = enablePrickOperation;
    }

    public void setVisibilityType(VisibilityType visibilityType) {
        this.visibilityType = visibilityType;
    }

    public VisibilityType getVisibilityType() {
        return visibilityType;
    }

    public void setEnableOutputSchedule(boolean enableOutputSchedule) {
        this.enableOutputSchedule = enableOutputSchedule;
    }

    public boolean isEnableOutputSchedule() {
        return enableOutputSchedule;
    }

    public void setEnableIncompatibleRelation(boolean enableIncompatibleRelation) {
        this.enableIncompatibleRelation = enableIncompatibleRelation;
    }

    public boolean isEnableIncompatibleRelation() {
        return enableIncompatibleRelation;
    }

    public static class Builder {
        private int searchMode = 0; // 0: dfs, 1: bfs, 2: h*
        private int searchLimit = -1;
        private int queueLimit = -1;
        private int visibilityLimit = -1; // -1: unlimited, 0:size of lin
        private boolean findAllAbstractExecution = false;
        private boolean enablePrickOperation = true;
        private boolean enableOutputSchedule = true;
        private boolean enableIncompatibleRelation = true;
        private VisibilityType visibilityType = VisibilityType.CAUSAL;
        private String adt = null;

        public Builder() {
            ;
        }

        public Builder(SearchConfiguration configuration) {
            this.searchMode = configuration.searchMode;
            this.searchLimit = configuration.searchLimit;
            this.queueLimit = configuration.queueLimit;
            this.visibilityLimit = configuration.visibilityLimit;
            this.findAllAbstractExecution = configuration.findAllAbstractExecution;
            this.enablePrickOperation = configuration.enablePrickOperation;
            this.enableOutputSchedule = configuration.enableOutputSchedule;
            this.enableIncompatibleRelation = configuration.enableIncompatibleRelation;
            this.visibilityType = configuration.visibilityType;
            this.adt = configuration.adt;
        }

        public Builder setSearchMode(int searchMode) {
            this.searchMode = searchMode;
            return this;
        }

        public Builder setSearchLimit(int searchLimit) {
            this.searchLimit = searchLimit;
            return this;
        }

        public Builder setQueueLimit(int queueLimit) {
            this.queueLimit = queueLimit;
            return this;
        }

        public Builder setVisibilityLimit(int visibilityLimit) {
            this.visibilityLimit = visibilityLimit;
            return this;
        }

        public Builder setFindAllAbstractExecution(boolean findAllAbstractExecution) {
            this.findAllAbstractExecution = findAllAbstractExecution;
            return this;
        }

        public Builder setEnablePrickOperation(boolean enablePrickOperation) {
            this.enablePrickOperation = enablePrickOperation;
            return this;
        }

        public Builder setEnableOutputSchedule(boolean enableOutputSchedule) {
            this.enableOutputSchedule = enableOutputSchedule;
            return this;
        }

        public Builder setEnableIncompatibleRelation(boolean enableIncompatibleRelation) {
            this.enableIncompatibleRelation = enableIncompatibleRelation;
            return this;
        }

        public Builder setVisibilityType(VisibilityType visibilityType) {
            this.visibilityType = visibilityType;
            return this;
        }

        public Builder setAdt(String adt) {
            this.adt = adt;
            return this;
        }

        public SearchConfiguration build() {
            return new SearchConfiguration(this);
        }
    }
}
