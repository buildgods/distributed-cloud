package org.cloud.common.enu;

public enum FileFolder {
    AVATAR("avatars"),TITLE("titles"),FILE("files");
    private String path;
    // 头像存储目录


    FileFolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static String getPath(String path){
        for (FileFolder fileFolder:FileFolder.values()) {
            if(fileFolder.getPath().equals(path)){
                return fileFolder.getPath();
            }
        }
        return null;
    }
}
