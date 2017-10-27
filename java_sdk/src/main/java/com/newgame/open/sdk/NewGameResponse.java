package com.newgame.open.sdk;

import java.util.Map;

/**
 * @author yakecanlee
 */
public class NewGameResponse {

    private int code;
    private Map content;

    private String rawContent;
    private Exception exception;

    public boolean isSuccessful() {
        return code == NewGameSdkConstants.CODE_SUCCESSFUL;
    }

    public boolean isServerError() {
        return code == NewGameSdkConstants.CODE_SERVER_ERROR;
    }

    public boolean isSystemError() {
        return code == NewGameSdkConstants.CODE_SYSTEM_ERROR;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Map getContent() {
        return content;
    }

    public void setContent(Map content) {
        this.content = content;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }
}
