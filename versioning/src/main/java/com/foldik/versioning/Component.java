package com.foldik.versioning;

import java.util.List;
import java.util.Objects;

public class Component {

    private String name;
    private String dir;
    private String version;
    private String nextVersion;
    private ChangeStatus changeStatus;
    private List<Component> dependencies;

    public Component(String name, String dir, String version, ChangeStatus changeStatus, List<Component> dependencies) {
        this.name = Objects.requireNonNull(name);
        this.dir = Objects.requireNonNull(dir);
        this.version = Objects.requireNonNull(version);
        this.nextVersion = Objects.requireNonNull(version);
        this.changeStatus = Objects.requireNonNull(changeStatus);
        this.dependencies = Objects.requireNonNull(dependencies);
    }

    public String getName() {
        return name;
    }

    public String getDir() {
        return dir;
    }

    public String getVersion() {
        return version;
    }

    public String getNextVersion() {
        return nextVersion;
    }

    public void toFixVersion() {
        if (version.endsWith("-SNAPSHOT") && changeStatus == ChangeStatus.NOT_CHANGED) {
            throw new RuntimeException(name + " should not be in SNAPSHOT version (" + version + ") since there was no change since the last version");
        }
        if (!version.endsWith("-SNAPSHOT") && changeStatus != ChangeStatus.NOT_CHANGED) {
            throw new RuntimeException(name + " should not be in FIX version (" + version + ") since there was change since the last version");
        }
        nextVersion = version.replace("-SNAPSHOT", "");
    }

    public ChangeStatus getChangeStatus() {
        return changeStatus;
    }

    public void setChangeStatus(final ChangeStatus changeStatus) {
        this.changeStatus = Objects.requireNonNull(changeStatus);
    }

    public List<Component> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", dir='" + dir + '\'' +
                ", version='" + version + '\'' +
                ", nextVersion='" + nextVersion + '\'' +
                ", changeStatus=" + changeStatus +
                ", dependencies=" + dependencies +
                '}';
    }
}
