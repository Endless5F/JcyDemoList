package com.android.framework.launch.data;

public class MixedResourceBean {
    private Title title;
    private ResourceInfo resourceInfo;

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void setResourceInfo(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
    }

    public static class Title {
        private String name;
        private int icon;
        private String time;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIcon() {
            return icon;
        }

        public void setIcon(int icon) {
            this.icon = icon;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }

    public static class ResourceInfo{
        private int thumbnail;
        private String thumbnailName;

        public int getThumbnail() {
            return thumbnail;
        }

        public void setThumbnail(int thumbnail) {
            this.thumbnail = thumbnail;
        }

        public String getThumbnailName() {
            return thumbnailName;
        }

        public void setThumbnailName(String thumbnailName) {
            this.thumbnailName = thumbnailName;
        }
    }
}
