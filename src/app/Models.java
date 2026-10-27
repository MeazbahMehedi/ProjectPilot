package app;

import java.util.*;

/** Persisted data objects. Capacity is derived from assignments, never decremented. */
public final class Models {
    public static final class User {
        public String id, name, major, role, password;
        User(String id,String name,String major,String role,String password) {
            this.id=id;this.name=name;this.major=major;this.role=role;this.password=password;
        }
    }
    public static final class Project {
        public int id, capacity, days;
        public String title, supervisor, category, description, deadline, status;
    }
    public static final class Event {
        public String actor,message,created;
        Event(String actor,String message,String created){this.actor=actor;this.message=message;this.created=created;}
    }
    public static final class Wait {
        public String user;
        public int project;
        Wait(String user,int project){this.user=user;this.project=project;}
    }
    public static final class Data {
        public int schemaVersion=1, nextId=1;
        public List<User> users=new ArrayList<>();
        public List<Project> projects=new ArrayList<>();
        public Map<String,Integer> assignments=new LinkedHashMap<>();
        public Map<String,Set<Integer>> favorites=new LinkedHashMap<>();
        public List<Wait> waitlist=new ArrayList<>();
        public List<Event> activity=new ArrayList<>();
    }
    private Models(){}
}
