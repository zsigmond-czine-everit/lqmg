package org.everit.lqmg;

public class MataDataGenerationParameter {
    private String changeLogFile;
    private String targetFolder;
    private String packageName = "";
    private boolean schemaToPackage = true;
    private String schemaPattern;

    public MataDataGenerationParameter(String changeLogFile, String targetFolder) {
        this.changeLogFile = changeLogFile;
        this.targetFolder = targetFolder;
    }

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public String getTargetFolder() {
        return targetFolder;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isSchemaToPackage() {
        return schemaToPackage;
    }

    public String getSchemaPattern() {
        return schemaPattern;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setSchemaToPackage(boolean schemaToPackage) {
        this.schemaToPackage = schemaToPackage;
    }

    public void setSchemaPattern(String schemaPattern) {
        this.schemaPattern = schemaPattern;
    }

}
