package com.cpbpc.comms;


import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class ComposerResult {

    private String fileName;
    private String script;
    private List<Tag> tags = new ArrayList<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void addTags(List<Tag> tags) {
        this.tags.addAll(tags);
    }

    public void addTag(Tag tag) {
        this.tags.add(tag);
    }
}
