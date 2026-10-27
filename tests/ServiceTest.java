package app;
import app.Models.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
public final class ServiceTest {
    static int checks=0;
    static void check(boolean condition,String name){if(!condition)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
    static void rejects(Runnable action,String name){try{action.run();throw new AssertionError("Expected rejection: "+name);}catch(IllegalArgumentException expected){checks++;System.out.println("PASS "+name);}}
    static Project project(String title,int capacity){Project p=new Project();p.title=title;p.supervisor="Dr Test";p.category="Testing";p.description="A test topic with useful details.";p.capacity=capacity;p.status="Open";p.deadline=LocalDate.now().plusDays(10).toString();return p;}
    public static void main(String[] args)throws Exception{
        Path dir=Files.createTempDirectory("workspace-tests-");
        try(Service s=new Service(dir)){
            s.login("TEACHER","DemoPass123!");check(s.user().get("role").equals("teacher"),"case insensitive login");rejects(()->s.login("teacher","wrong"),"incorrect password");
            s.register("new","New Student","CS","LongPass123!");rejects(()->s.register("NEW","X","CS","LongPass123!"),"duplicate account");rejects(()->s.register("bad id","X","CS","LongPass123!"),"invalid account ID");rejects(()->s.register("weak","X","CS","short"),"weak password");
            int a=s.saveProject(project("First",1),null),b=s.saveProject(project("Second",1),null);Project draft=project("Draft",2);draft.status="Draft";int draftId=s.saveProject(draft,null);
            s.login("student","DemoPass123!");check(s.projects().stream().noneMatch(p->p.id==draftId),"draft hidden from student");rejects(()->s.deleteProject(a),"student cannot delete");check(s.students().size()==1,"student record scope");
            s.select(a);check(s.available(a)==0,"capacity accounting");rejects(()->s.select(a),"duplicate selection rejected");
            s.login("student2","DemoPass123!");int old=s.selected();rejects(()->s.select(a),"full project rejected");check(s.selected()==old,"failed switch preserves assignment");s.cancel();s.waitlist(a);check(s.waiting(a),"join waitlist");
            s.login("student","DemoPass123!");s.select(b);check(s.available(a)==0,"released seat filled from waitlist");
            s.login("student2","DemoPass123!");check(s.selected()==a&&!s.waiting(a),"waitlist promoted automatically");s.cancel();check(s.available(a)==1,"cancellation releases seat");s.favorite(a);check(s.saved(a),"save topic");s.favorite(a);check(!s.saved(a),"unsave topic");check(s.activity().stream().allMatch(e->e.actor.equals("student2")),"activity privacy");
            s.login("teacher","DemoPass123!");Project expired=project("Expired",1);expired.deadline=LocalDate.now().minusDays(1).toString();int e=s.saveProject(expired,null);Project closed=project("Closed",1);closed.status="Closed";int c=s.saveProject(closed,null);s.login("new","LongPass123!");rejects(()->s.select(e),"expired blocked");rejects(()->s.select(c),"closed blocked");
            Path export=dir.resolve("export.json");s.export(export,"json");String output=Files.readString(export);check(!output.contains("password")&&!output.contains("DemoPass"),"exports omit credentials");rejects(()->{try{s.export(dir.resolve("workspace.json"),"json");}catch(java.io.IOException ex){throw new RuntimeException(ex);}},"cannot export over live store");
            s.login("teacher","DemoPass123!");Project invalid=project("Invalid",0);rejects(()->s.saveProject(invalid,null),"invalid capacity");Project occupied=s.projects().stream().filter(p->p.id==b).findFirst().orElseThrow();occupied.status="Draft";rejects(()->s.saveProject(occupied,b),"assigned project cannot become draft");
            int formula=s.saveProject(project("=1+1",1),null);Path csv=dir.resolve("export.csv");s.export(csv,"csv");check(Files.readString(csv).contains("'=1+1"),"CSV formula escaping");
            s.deleteProject(b);s.login("student","DemoPass123!");check(s.selected()==null,"deletion releases students");s.login("teacher","DemoPass123!");s.backup(dir.resolve("backup.json"));check(Files.exists(dir.resolve("backup.json")),"backup created");
            s.changePassword("DemoPass123!","NewPass123!");rejects(()->s.login("teacher","DemoPass123!"),"old password revoked");s.login("teacher","NewPass123!");check(true,"new password accepted");
            try(Service other=new Service(dir)){throw new AssertionError("Second instance opened");}catch(java.io.IOException|java.nio.channels.OverlappingFileLockException expected){check(true,"workspace process lock");}
        }
        try(Service s=new Service(dir)){s.login("teacher","NewPass123!");check(s.projects().stream().anyMatch(p->p.title.equals("First")),"saved data survives restart");}
        Path broken=Files.createTempDirectory("broken-workspace-");Files.writeString(broken.resolve("workspace.json"),"{broken");try(Service s=new Service(broken)){throw new AssertionError("Corrupt data silently reset");}catch(java.io.IOException expected){check(Files.readString(broken.resolve("workspace.json")).equals("{broken"),"corrupt file preserved");}
        try(var paths=Files.walk(dir)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}try(var paths=Files.walk(broken)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(p);}
        System.out.println("SUCCESS: "+checks+" checks passed.");
    }
}
