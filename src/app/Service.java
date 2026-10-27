package app;

import app.Models.*;
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.time.*;
import java.util.*;
import java.util.function.*;

/** Business operations: role checks, input validation, assignments and reporting. */
public final class Service implements AutoCloseable {
    private final Store store;
    private Data data;
    private User session;
    public static final int ITERATIONS=210000;
    public Service(Path directory) throws Exception {
        store=new Store(directory);
        try {data=store.load();if(data==null){data=seed();store.save(data);}}
        catch(Exception ex){store.close();throw ex;}
    }
    private Data seed() throws Exception {
        Data d=new Data();
        String[][] users={{"teacher","Demo Supervisor","Faculty","teacher"},{"student","Demo Student","Software Engineering","student"},{"student2","Alex Chen","Computer Science","student"},{"student3","Sam Lee","Information Systems","student"}};
        for(String[] u:users)d.users.add(new User(u[0],u[1],u[2],u[3],hash("DemoPass123!",null)));
        try(InputStream in=Service.class.getResourceAsStream("/seed.json")){
            if(in==null)throw new IOException("Missing seed.json resource; rebuild the app.");
            Project[] ps=store.gson.fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Project[].class);
            for(Project p:ps){p.id=d.nextId++;p.deadline=LocalDate.now().plusDays(p.days).toString();d.projects.add(p);}
        }
        d.assignments.put("student2",1);d.assignments.put("student3",3);
        d.activity.add(new Event("system","Demo workspace initialized",now()));return d;
    }
    static String now(){return LocalDateTime.now().withNano(0).toString();}
    static String hash(String password,String salt) {
        try {
            byte[] s=salt==null?new byte[16]:HexFormat.of().parseHex(salt);
            if(salt==null)new SecureRandom().nextBytes(s);
            PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),s,ITERATIONS,256);
            try{return HexFormat.of().formatHex(s)+":"+HexFormat.of().formatHex(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());}
            finally{spec.clearPassword();}
        }catch(GeneralSecurityException ex){throw new IllegalStateException(ex);}
    }
    static boolean verify(String password,String encoded){return MessageDigest.isEqual(hash(password,encoded.split(":")[0]).getBytes(StandardCharsets.UTF_8),encoded.getBytes(StandardCharsets.UTF_8));}
    static String required(String value,String label,int max){if(value==null||value.isBlank()||value.trim().length()>max)throw new IllegalArgumentException(label+" is required (maximum "+max+" characters).");return value.trim();}
    static void validPassword(String p){if(p.length()<8||p.length()>128)throw new IllegalArgumentException("Use a password with 8–128 characters.");}
    private void auth(boolean teacher){if(session==null)throw new IllegalArgumentException("Sign in first.");if(teacher&&!session.role.equals("teacher"))throw new IllegalArgumentException("Only supervisors can do this.");}
    private void student(){auth(false);if(!session.role.equals("student"))throw new IllegalArgumentException("Only students can do this.");}
    public Map<String,String> user(){auth(false);return publicUser(session);}
    private Map<String,String> publicUser(User u){return Map.of("id",u.id,"name",u.name,"major",u.major,"role",u.role);}
    public void logout(){session=null;}
    public void login(String id,String password){
        User u=data.users.stream().filter(v->v.id.equalsIgnoreCase(id.trim())).findFirst().orElse(null);
        if(u==null||!verify(password,u.password))throw new IllegalArgumentException("Incorrect account ID or password.");session=u;
    }
    private void transaction(Runnable action){
        String snapshot=store.gson.toJson(data);String sid=session==null?null:session.id;
        try{action.run();store.save(data);}
        catch(Exception ex){data=store.gson.fromJson(snapshot,Data.class);if(sid!=null)session=data.users.stream().filter(u->u.id.equals(sid)).findFirst().orElse(null);throw ex instanceof RuntimeException?(RuntimeException)ex:new IllegalStateException("Could not save the workspace: "+ex.getMessage(),ex);}
    }
    private void log(String actor,String message){data.activity.add(new Event(actor,message,now()));}
    public void register(String id,String name,String major,String password){
        String uid=required(id,"Account ID",40),n=required(name,"Name",120),m=required(major,"Major",120);validPassword(password);
        if(!uid.matches("[\\p{L}\\p{N}._-]+"))throw new IllegalArgumentException("Use letters, numbers, dot, underscore or hyphen for the account ID.");
        if(data.users.stream().anyMatch(u->u.id.equalsIgnoreCase(uid)))throw new IllegalArgumentException("That account ID is already registered.");
        transaction(()->{data.users.add(new User(uid,n,m,"student",hash(password,null)));log(uid,"Student account created");});
    }
    public void changePassword(String old,String next){auth(false);validPassword(next);if(!verify(old,session.password))throw new IllegalArgumentException("Current password is incorrect.");transaction(()->{session.password=hash(next,null);log(session.id,"Password changed");});}
    private Project find(int id){return data.projects.stream().filter(p->p.id==id).findFirst().orElseThrow(()->new IllegalArgumentException("Project no longer exists."));}
    public int enrolled(int id){auth(false);return (int)data.assignments.values().stream().filter(p->p==id).count();}
    public int available(int id){auth(false);return find(id).capacity-enrolled(id);}
    public boolean saved(int id){auth(false);return data.favorites.getOrDefault(session.id,Set.of()).contains(id);}
    public boolean waiting(int id){auth(false);return data.waitlist.stream().anyMatch(w->w.project==id&&w.user.equals(session.id));}
    public Integer selected(){auth(false);return data.assignments.get(session.id);}
    public List<Project> projects(){auth(false);List<Project> ps=new ArrayList<>();for(Project p:data.projects)if(!p.status.equals("Draft")||session.role.equals("teacher"))ps.add(store.gson.fromJson(store.gson.toJson(p),Project.class));return ps;}
    public List<Map<String,Object>> students(){
        auth(false);List<Map<String,Object>> rows=new ArrayList<>();
        for(User u:data.users)if(u.role.equals("student")&&(session.role.equals("teacher")||u.id.equals(session.id))){
            Map<String,Object> r=new LinkedHashMap<>(publicUser(u));r.remove("role");Integer pid=data.assignments.get(u.id);r.put("project_id",pid);r.put("project",pid==null?"Unassigned":find(pid).title);rows.add(r);
        }rows.sort(Comparator.comparing(r->r.get("name").toString()));return rows;
    }
    public List<Event> activity(){auth(false);List<Event> rows=new ArrayList<>();for(int i=data.activity.size()-1;i>=0&&rows.size()<80;i--){Event e=data.activity.get(i);if(session.role.equals("teacher")||e.actor.equals(session.id))rows.add(new Event(e.actor,e.message,e.created));}return rows;}
    public int saveProject(Project input,Integer id){
        auth(true);Project p=store.gson.fromJson(store.gson.toJson(input),Project.class);
        p.title=required(p.title,"Title",120);p.supervisor=required(p.supervisor,"Supervisor",120);p.category=required(p.category,"Category",120);p.description=required(p.description,"Description",3000);
        if(p.capacity<1||p.capacity>10000)throw new IllegalArgumentException("Capacity must be between 1 and 10,000.");
        try{LocalDate.parse(p.deadline);}catch(Exception ex){throw new IllegalArgumentException("Use a valid deadline: YYYY-MM-DD.");}
        if(!List.of("Draft","Open","Closed").contains(p.status))throw new IllegalArgumentException("Invalid project status.");
        if(id!=null){find(id);if(p.capacity<enrolled(id))throw new IllegalArgumentException("Capacity cannot be below current enrollment.");if(p.status.equals("Draft")&&enrolled(id)>0)throw new IllegalArgumentException("An assigned project cannot be changed to Draft. Use Closed instead.");p.id=id;}else p.id=data.nextId;
        transaction(()->{if(id==null)data.nextId++;else data.projects.removeIf(v->v.id==id);data.projects.add(p);data.projects.sort(Comparator.comparingInt(v->v.id));promote(p.id);log(session.id,"Saved: "+p.title);});return p.id;
    }
    public void deleteProject(int id){auth(true);Project p=find(id);transaction(()->{data.projects.removeIf(v->v.id==id);data.assignments.values().removeIf(v->v==id);data.waitlist.removeIf(w->w.project==id);data.favorites.values().forEach(s->s.remove(id));log(session.id,"Deleted project and released its students: "+p.title);});}
    private boolean open(Project p){return p.status.equals("Open")&&!LocalDate.parse(p.deadline).isBefore(LocalDate.now());}
    private Project openProject(int id){Project p=find(id);if(!open(p))throw new IllegalArgumentException("This project is closed or its deadline has passed.");return p;}
    public void select(int id){
        student();Project p=openProject(id);Integer old=selected();if(old!=null&&old==id)throw new IllegalArgumentException("You already selected this project.");if(available(id)<=0)throw new IllegalArgumentException("This project is full. You can join its waitlist.");
        transaction(()->{data.assignments.put(session.id,id);data.waitlist.removeIf(w->w.user.equals(session.id));if(old!=null)promote(old);log(session.id,"Selected: "+p.title);});
    }
    public void cancel(){student();Integer old=selected();if(old==null)throw new IllegalArgumentException("You have no active selection.");transaction(()->{data.assignments.remove(session.id);log(session.id,"Cancelled project selection");promote(old);});}
    public void waitlist(int id){
        student();Project p=openProject(id);
        if(waiting(id)){transaction(()->{data.waitlist.removeIf(w->w.user.equals(session.id)&&w.project==id);log(session.id,"Left waitlist: "+p.title);});return;}
        if(selected()!=null)throw new IllegalArgumentException("Cancel your current selection before joining a waitlist.");
        if(available(id)>0)throw new IllegalArgumentException("Seats are available. Select this project directly.");
        transaction(()->{data.waitlist.add(new Wait(session.id,id));log(session.id,"Joined waitlist: "+p.title);});
    }
    private void promote(int id){Project p=find(id);if(!open(p))return;while(available(id)>0){Wait next=data.waitlist.stream().filter(w->w.project==id).findFirst().orElse(null);if(next==null)break;data.waitlist.remove(next);if(data.assignments.containsKey(next.user))continue;data.assignments.put(next.user,id);data.waitlist.removeIf(w->w.user.equals(next.user));log(next.user,"Automatically assigned from waitlist: "+p.title);}}
    public void favorite(int id){auth(false);if(projects().stream().noneMatch(p->p.id==id))throw new IllegalArgumentException("Project is not available.");transaction(()->{Set<Integer> s=data.favorites.computeIfAbsent(session.id,k->new LinkedHashSet<>());if(s.contains(id))s.remove(id);else s.add(id);});}
    public void backup(Path target) throws IOException{auth(true);if(target.toAbsolutePath().normalize().equals(store.path.toAbsolutePath().normalize()))throw new IllegalArgumentException("Choose a different file for the backup.");Files.copy(store.path,target,StandardCopyOption.REPLACE_EXISTING);}
    private String csv(Object value){String s=String.valueOf(value);if(!s.isEmpty()&&"=+-@\t\r".indexOf(s.charAt(0))>=0)s="'"+s;return "\""+s.replace("\"","\"\"")+"\"";}
    public void export(Path target,String kind) throws IOException {
        auth(false);if(target.toAbsolutePath().normalize().equals(store.path.toAbsolutePath().normalize()))throw new IllegalArgumentException("An export cannot replace the live workspace.");List<Map<String,Object>> ps=new ArrayList<>();
        for(Project p:projects()){Map<String,Object> r=new LinkedHashMap<>();r.put("id",p.id);r.put("title",p.title);r.put("supervisor",p.supervisor);r.put("category",p.category);r.put("description",p.description);r.put("capacity",p.capacity);r.put("deadline",p.deadline);r.put("status",p.status);r.put("enrolled",enrolled(p.id));r.put("available",available(p.id));ps.add(r);}
        if(kind.equals("json")){Map<String,Object> result=new LinkedHashMap<>();result.put("projects",ps);result.put("students",students());result.put("activity",activity());Files.writeString(target,store.gson.toJson(result),StandardCharsets.UTF_8);}
        else {StringBuilder b=new StringBuilder("\ufeff");List<String> keys=List.of("id","title","supervisor","category","description","capacity","deadline","status","enrolled","available");b.append(String.join(",",keys)).append('\n');for(Map<String,Object> r:ps){for(int i=0;i<keys.size();i++){if(i>0)b.append(',');b.append(csv(r.get(keys.get(i))));}b.append('\n');}Files.writeString(target,b,StandardCharsets.UTF_8);}
    }
    public void close() throws IOException{store.close();}
}
