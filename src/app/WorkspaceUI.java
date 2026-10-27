package app;

import app.Models.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.List;

/** Desktop presentation. Business actions are delegated to Service. */
public final class WorkspaceUI extends JPanel {
    final Service service;
    boolean dark=true;
    String page="Overview",query="",category="All categories";
    boolean favoritesOnly=false;
    Color bg,panel,text,muted,line,soft,accent=new Color(Config.ACCENT),mint=new Color(0x55cbb5);
    JPanel content;
    JTable projectTable;
    JTextField search;
    JComboBox<String> categories;
    JLabel counter;
    List<Project> visible=new ArrayList<>();
    public WorkspaceUI(Service service){
        this.service=service;setLayout(new BorderLayout());colors();loginScreen();
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"),"search");
        getActionMap().put("search",new AbstractAction(){public void actionPerformed(ActionEvent e){try{service.user();navigate("Projects");search.requestFocusInWindow();}catch(Exception ignored){}}});
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"),"refresh");
        getActionMap().put("refresh",new AbstractAction(){public void actionPerformed(ActionEvent e){try{service.user();render();}catch(Exception ignored){}}});
    }
    void colors(){
        bg=new Color(dark?0x10131e:0xf2f4fa);panel=new Color(dark?0x191e2d:0xffffff);text=new Color(dark?0xedf0fa:0x17213a);muted=new Color(dark?0x9ba7bf:0x5d6b83);line=new Color(dark?0x2a3248:0xdce2ef);soft=new Color(dark?0x232a3e:0xeaf0fb);setBackground(bg);
        for(String key:List.of("Panel.background","OptionPane.background","Viewport.background"))UIManager.put(key,panel);
        UIManager.put("OptionPane.messageForeground",text);UIManager.put("Label.foreground",text);
        UIManager.put("ComboBox.background",soft);UIManager.put("ComboBox.foreground",text);
        UIManager.put("TextField.background",soft);UIManager.put("TextField.foreground",text);
    }
    JLabel label(String value,int size,Color color,boolean bold){JLabel l=new JLabel(value);l.setForeground(color);l.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));return l;}
    JPanel vertical(Color color){JPanel p=new JPanel();p.setBackground(color);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));return p;}
    JPanel flow(Color color){JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));p.setBackground(color);return p;}
    JPanel card(){JPanel p=vertical(panel);p.setBorder(new CompoundBorder(new LineBorder(line),new EmptyBorder(21,22,21,22)));return p;}
    void gap(JPanel p,int n){p.add(Box.createVerticalStrut(n));}
    void addLabel(JPanel p,String value,int size,Color color,boolean bold){JLabel l=label(value,size,color,bold);l.setAlignmentX(LEFT_ALIGNMENT);p.add(l);}
    JButton button(String title,Runnable action,boolean primary){
        JButton b=new JButton(title){protected void paintComponent(Graphics g){Graphics2D gg=(Graphics2D)g.create();gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);gg.setColor(getModel().isRollover()?accent:(primary?accent:soft));gg.fillRoundRect(0,0,getWidth(),getHeight(),12,12);gg.dispose();super.paintComponent(g);}};
        b.setContentAreaFilled(false);b.setOpaque(false);b.setFocusPainted(false);b.setBorder(new EmptyBorder(12,16,12,16));b.setForeground(primary?Color.WHITE:text);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.addActionListener(e->guard(action));b.setAlignmentX(LEFT_ALIGNMENT);return b;
    }
    void guard(Runnable action){try{action.run();}catch(IllegalArgumentException ex){JOptionPane.showMessageDialog(this,ex.getMessage(),Config.NAME,JOptionPane.WARNING_MESSAGE);}catch(Exception ex){JOptionPane.showMessageDialog(this,"The operation could not be completed.\n"+ex.getMessage(),Config.NAME,JOptionPane.ERROR_MESSAGE);}}
    JTextField field(String value,boolean secret){JTextField f=secret?new JPasswordField(value):new JTextField(value);f.setFont(new Font("SansSerif",Font.PLAIN,14));f.setBackground(soft);f.setForeground(text);f.setCaretColor(text);f.setBorder(new CompoundBorder(new LineBorder(line),new EmptyBorder(10,10,10,10)));return f;}
    void finish(){revalidate();repaint();}
    void loginScreen(){
        removeAll();setLayout(new BorderLayout());colors();
        JPanel shell=new JPanel(new GridLayout(1,2,65,0));shell.setBackground(bg);shell.setBorder(new EmptyBorder(65,65,65,65));add(shell);
        JPanel intro=vertical(bg);shell.add(intro);gap(intro,25);addLabel(intro,Config.NAME.substring(0,1)+" /  "+Config.NAME,28,accent,true);gap(intro,75);
        addLabel(intro,"Good ideas deserve",36,text,true);addLabel(intro,"a great team.",36,text,true);gap(intro,25);
        addLabel(intro,"Explore topics. Find your focus.",18,muted,false);gap(intro,5);addLabel(intro,"Build something that matters.",18,muted,false);gap(intro,45);
        addLabel(intro,"01 DISCOVER   /   02 SELECT   /   03 CREATE",12,mint,true);intro.add(Box.createVerticalGlue());addLabel(intro,Config.EDITION+" edition / Local desktop workspace",12,muted,false);
        JPanel wrap=new JPanel(new GridBagLayout());wrap.setBackground(bg);shell.add(wrap);JPanel form=card();form.setPreferredSize(new Dimension(390,530));wrap.add(form);
        addLabel(form,"Welcome back",28,text,true);gap(form,8);addLabel(form,"Sign in to your academic workspace.",13,muted,false);gap(form,30);
        addLabel(form,"Account ID",12,muted,false);gap(form,8);JTextField id=field("",false);id.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));id.setAlignmentX(LEFT_ALIGNMENT);form.add(id);gap(form,18);
        addLabel(form,"Password",12,muted,false);gap(form,8);JTextField pass=field("",true);pass.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));pass.setAlignmentX(LEFT_ALIGNMENT);form.add(pass);gap(form,22);
        Runnable go=()->{service.login(id.getText(),pass.getText());page="Overview";render();};JButton login=button("Sign in  →",go,true);login.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));form.add(login);gap(form,10);
        JButton register=button("Create student account",this::registerDialog,false);register.setMaximumSize(new Dimension(Integer.MAX_VALUE,44));form.add(register);gap(form,28);
        addLabel(form,"Try the included demo workspace",13,text,true);gap(form,9);addLabel(form,"teacher / DemoPass123!",12,muted,false);gap(form,5);addLabel(form,"student / DemoPass123!",12,muted,false);gap(form,12);
        addLabel(form,"Demo data is fictional. Change passwords",11,muted,false);addLabel(form,"before using real records.",11,muted,false);
        pass.addActionListener(e->guard(go));finish();
    }
    interface Submit{void run(Map<String,String> values);}
    void formDialog(String title,String[][] fields,Map<String,String[]> choices,Submit submit){
        Window owner=SwingUtilities.getWindowAncestor(this);JDialog d=new JDialog(owner,title,Dialog.ModalityType.APPLICATION_MODAL);JPanel box=new JPanel(new GridBagLayout());box.setBackground(panel);box.setBorder(new EmptyBorder(22,26,22,26));d.setContentPane(box);
        GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(6,6,6,6);c.fill=GridBagConstraints.HORIZONTAL;c.gridx=0;c.gridy=0;c.gridwidth=2;box.add(label(title,23,text,true),c);c.gridwidth=1;
        Map<String,JComponent> controls=new LinkedHashMap<>();int row=1;
        for(String[] f:fields){c.gridy=row++;c.gridx=0;c.weightx=0;box.add(label(f[1],12,muted,false),c);c.gridx=1;c.weightx=1;JComponent input;
            if(choices.containsKey(f[0])){JComboBox<String> v=new JComboBox<>(choices.get(f[0]));v.setSelectedItem(f[2]);input=v;}
            else if(f[0].equals("description")){JTextArea area=new JTextArea(f[2],4,32);area.setLineWrap(true);area.setWrapStyleWord(true);area.setBackground(soft);area.setForeground(text);area.setCaretColor(text);area.setBorder(new EmptyBorder(10,10,10,10));input=new JScrollPane(area);}
            else{JTextField v=field(f[2],f.length>3&&f[3].equals("secret"));v.setColumns(29);input=v;}
            input.setFont(new Font("SansSerif",Font.PLAIN,13));controls.put(f[0],input);box.add(input,c);
        }
        c.gridy=row;c.gridx=0;c.gridwidth=2;JPanel buttons=flow(panel);
        buttons.add(button("Cancel",d::dispose,false));buttons.add(button("Save",()->{Map<String,String> values=new LinkedHashMap<>();for(var e:controls.entrySet()){JComponent v=e.getValue();String val=v instanceof JComboBox?Objects.toString(((JComboBox<?>)v).getSelectedItem(),""):v instanceof JScrollPane?((JTextArea)((JScrollPane)v).getViewport().getView()).getText():((JTextField)v).getText();values.put(e.getKey(),val);}submit.run(values);d.dispose();try{service.user();render();}catch(Exception ignored){}},true));box.add(buttons,c);
        d.pack();d.setMinimumSize(d.getSize());d.setLocationRelativeTo(this);d.setVisible(true);
    }
    void registerDialog(){formDialog("Create your student account",new String[][]{{"id","Account ID",""},{"name","Full name",""},{"major","Major","Software Engineering"},{"password","Password (8+ characters)","","secret"}},Map.of(),v->{service.register(v.get("id"),v.get("name"),v.get("major"),v.get("password"));JOptionPane.showMessageDialog(this,"Account created. You can now sign in.");});}
    void navigate(String next){page=next;query="";category="All categories";favoritesOnly=false;render();}
    void toggleTheme(){dark=!dark;colors();render();}
    void render(){
        removeAll();setLayout(new BorderLayout());colors();Map<String,String> u=service.user();
        JPanel side=vertical(panel);side.setBorder(new EmptyBorder(32,16,20,16));side.setPreferredSize(new Dimension(220,700));add(side,BorderLayout.WEST);
        addLabel(side,Config.NAME,23,accent,true);gap(side,10);addLabel(side,"PROJECT WORKSPACE",10,muted,true);gap(side,38);
        for(String target:List.of("Overview","Projects",u.get("role").equals("teacher")?"Students":"My selection","Activity","Settings")){JButton b=button(target,()->navigate(target),page.equals(target));b.setMaximumSize(new Dimension(Integer.MAX_VALUE,48));side.add(b);gap(side,10);}
        side.add(Box.createVerticalGlue());addLabel(side,u.get("role").equals("teacher")?"SUPERVISOR":"STUDENT",11,mint,true);gap(side,14);addLabel(side,Config.EDITION+" / v2.0.0",11,muted,false);gap(side,20);side.add(button("Sign out",()->{service.logout();query="";loginScreen();},false));
        JPanel outer=new JPanel(new BorderLayout(0,20));outer.setBackground(bg);outer.setBorder(new EmptyBorder(27,30,22,30));add(outer,BorderLayout.CENTER);
        JPanel header=new JPanel(new BorderLayout());header.setBackground(bg);JPanel titles=vertical(bg);addLabel(titles,"WORKSPACE / "+page.toUpperCase(),10,muted,true);gap(titles,9);addLabel(titles,page,30,text,true);header.add(titles,BorderLayout.WEST);
        JPanel actions=flow(bg);actions.add(label(u.get("name"),13,muted,false));actions.add(button(dark?"Light mode":"Dark mode",this::toggleTheme,false));header.add(actions,BorderLayout.EAST);outer.add(header,BorderLayout.NORTH);
        content=new JPanel(new BorderLayout(0,16));content.setBackground(bg);outer.add(content,BorderLayout.CENTER);
        outer.add(label("LOCAL WORKSPACE  •  Changes save automatically     |     "+LocalDate.now(),10,muted,false),BorderLayout.SOUTH);
        switch(page){case "Overview"->overview();case "Projects"->projectsPage();case "Students"->studentsPage();case "My selection"->selectionPage();case "Activity"->activityPage();default->settingsPage();}finish();
    }
    void overview(){
        List<Project> ps=service.projects();List<Map<String,Object>> students=service.students();boolean teacher=service.user().get("role").equals("teacher");
        JPanel upper=vertical(bg);JPanel hero=card();hero.setAlignmentX(LEFT_ALIGNMENT);addLabel(hero,"YOUR NEXT CHAPTER STARTS HERE",11,mint,true);gap(hero,12);addLabel(hero,"Turn curiosity into a project.",29,text,true);gap(hero,10);addLabel(hero,"A clear view of your topics, teams, and next steps.",14,muted,false);gap(hero,20);hero.add(button("Explore the project catalog  →",()->navigate("Projects"),true));upper.add(hero);gap(upper,18);
        JPanel metrics=new JPanel(new GridLayout(1,3,14,0));metrics.setBackground(bg);metrics.setAlignmentX(LEFT_ALIGNMENT);int seats=ps.stream().filter(p->p.status.equals("Open")&&!LocalDate.parse(p.deadline).isBefore(LocalDate.now())).mapToInt(p->service.available(p.id)).sum();
        String[] names={"PROJECT TOPICS","AVAILABLE SEATS",teacher?"STUDENTS":"MY ASSIGNMENT"};String[] values={""+ps.size(),""+seats,teacher?""+students.size():service.selected()==null?"Not yet":"Selected"};Color[] colors={accent,mint,new Color(0xe9b76d)};
        for(int i=0;i<3;i++){JPanel m=card();addLabel(m,names[i],11,muted,true);gap(m,12);addLabel(m,values[i],30,colors[i],true);metrics.add(m);}upper.add(metrics);content.add(upper,BorderLayout.NORTH);
        JPanel lower=new JPanel(new GridLayout(1,2,16,0));lower.setBackground(bg);content.add(lower);JPanel chart=card();lower.add(chart);addLabel(chart,"Seats by topic",18,text,true);gap(chart,7);addLabel(chart,"Assigned / total capacity",12,muted,false);gap(chart,15);
        for(Project p:ps.subList(0,Math.min(4,ps.size()))){JPanel row=new JPanel(new BorderLayout());row.setBackground(panel);row.setAlignmentX(LEFT_ALIGNMENT);row.add(label(p.title,12,text,false),BorderLayout.WEST);row.add(label(service.enrolled(p.id)+" / "+p.capacity,11,muted,false),BorderLayout.EAST);chart.add(row);gap(chart,7);JProgressBar bar=new JProgressBar(0,p.capacity);bar.setValue(service.enrolled(p.id));bar.setBackground(soft);bar.setForeground(accent);bar.setBorderPainted(false);bar.setPreferredSize(new Dimension(200,6));bar.setMaximumSize(new Dimension(Integer.MAX_VALUE,6));bar.setAlignmentX(LEFT_ALIGNMENT);chart.add(bar);gap(chart,14);}chart.add(Box.createVerticalGlue());
        JPanel recent=card();lower.add(recent);addLabel(recent,"Recent activity",18,text,true);gap(recent,17);for(Models.Event e:service.activity().subList(0,Math.min(4,service.activity().size()))){addLabel(recent,e.message,12,text,false);gap(recent,5);addLabel(recent,e.created.replace('T',' '),11,muted,false);gap(recent,15);}recent.add(Box.createVerticalGlue());
    }
    JPanel tablePanel(JTable table){
        table.setFont(new Font("SansSerif",Font.PLAIN,13));table.setRowHeight(46);table.setBackground(panel);table.setForeground(text);table.setSelectionBackground(accent);table.setSelectionForeground(Color.WHITE);table.setGridColor(line);table.setShowVerticalLines(false);table.setShowHorizontalLines(true);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setAutoCreateRowSorter(true);table.getTableHeader().setBackground(soft);table.getTableHeader().setForeground(muted);table.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,11));table.getTableHeader().setPreferredSize(new Dimension(100,44));table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class,new DefaultTableCellRenderer(){public Component getTableCellRendererComponent(JTable t,Object v,boolean selected,boolean focus,int r,int c){super.getTableCellRendererComponent(t,v,selected,focus,r,c);setBorder(new EmptyBorder(0,12,0,8));return this;}});
        JScrollPane scroll=new JScrollPane(table);scroll.setBorder(new LineBorder(line));scroll.getViewport().setBackground(panel);JPanel wrapper=new JPanel(new BorderLayout());wrapper.setBackground(bg);wrapper.add(scroll);return wrapper;
    }
    DefaultTableModel model(String[] columns){return new DefaultTableModel(columns,0){public boolean isCellEditable(int row,int col){return false;}};}
    void projectsPage(){
        JPanel top=vertical(bg);addLabel(top,"Discover a topic, check availability, and find your next team.",13,muted,false);gap(top,18);
        JPanel tools=new JPanel(new BorderLayout(10,0));tools.setBackground(bg);tools.setAlignmentX(LEFT_ALIGNMENT);search=field(query,false);search.setToolTipText("Search topics, supervisors, categories and descriptions");search.getAccessibleContext().setAccessibleName("Search project catalog");tools.add(search,BorderLayout.CENTER);JPanel right=flow(bg);List<String> cats=new ArrayList<>();cats.add("All categories");service.projects().stream().map(p->p.category).distinct().sorted().forEach(cats::add);categories=new JComboBox<>(cats.toArray(String[]::new));categories.setSelectedItem(category);categories.setPreferredSize(new Dimension(200,42));right.add(categories);right.add(button(favoritesOnly?"★ Saved":"☆ Saved",()->{favoritesOnly=!favoritesOnly;query=search.getText();category=Objects.toString(categories.getSelectedItem());render();},false));if(service.user().get("role").equals("teacher"))right.add(button("+ New project",()->projectDialog(null),true));tools.add(right,BorderLayout.EAST);top.add(tools);content.add(top,BorderLayout.NORTH);
        projectTable=new JTable(model(new String[]{"PROJECT TITLE","SUPERVISOR","CATEGORY","SEATS","DEADLINE","STATUS"}));projectTable.getColumnModel().getColumn(0).setPreferredWidth(260);projectTable.getColumnModel().getColumn(1).setPreferredWidth(115);projectTable.getColumnModel().getColumn(2).setPreferredWidth(150);projectTable.getColumnModel().getColumn(3).setPreferredWidth(65);projectTable.getColumnModel().getColumn(4).setPreferredWidth(105);projectTable.getColumnModel().getColumn(5).setPreferredWidth(70);content.add(tablePanel(projectTable));
        JPanel bottom=vertical(bg);counter=label("",12,muted,false);bottom.add(counter);gap(bottom,15);JPanel buttons=flow(bg);buttons.add(button("View details / actions",()->details(selectedProject()),true));buttons.add(button("Export CSV",()->export("csv"),false));buttons.add(button("Export JSON",()->export("json"),false));bottom.add(buttons);content.add(bottom,BorderLayout.SOUTH);
        projectTable.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2)guard(()->details(selectedProject()));}});
        search.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){filter();}public void removeUpdate(DocumentEvent e){filter();}public void changedUpdate(DocumentEvent e){filter();}});categories.addActionListener(e->filter());filter();
    }
    void filter(){
        query=search.getText();category=Objects.toString(categories.getSelectedItem());visible.clear();DefaultTableModel m=(DefaultTableModel)projectTable.getModel();m.setRowCount(0);
        for(Project p:service.projects()){String searchable=(p.title+" "+p.supervisor+" "+p.category+" "+p.description).toLowerCase(Locale.ROOT);if(!searchable.contains(query.toLowerCase(Locale.ROOT)))continue;if(!category.equals("All categories")&&!category.equals(p.category))continue;if(favoritesOnly&&!service.saved(p.id))continue;visible.add(p);String status=p.status.equals("Open")&&LocalDate.parse(p.deadline).isBefore(LocalDate.now())?"Expired":p.status;m.addRow(new Object[]{(service.saved(p.id)?"★ ":"")+p.title,p.supervisor,p.category,service.available(p.id)+"/"+p.capacity,p.deadline,status});}
        counter.setText(visible.size()+" topics   •   Double-click a row for details   •   Ctrl+F to search");
    }
    Project selectedProject(){int row=projectTable.getSelectedRow();if(row<0)throw new IllegalArgumentException("Select a project row first.");return visible.get(projectTable.convertRowIndexToModel(row));}
    void details(Project p){
        JDialog d=new JDialog(SwingUtilities.getWindowAncestor(this),p.title,Dialog.ModalityType.APPLICATION_MODAL);JPanel box=card();d.setContentPane(box);addLabel(box,p.category.toUpperCase(),12,mint,true);gap(box,15);addLabel(box,p.title,25,text,true);gap(box,15);addLabel(box,p.supervisor+"   •   "+p.status+"   •   Due "+p.deadline,13,muted,false);gap(box,18);addLabel(box,service.available(p.id)+" seats available / "+p.capacity+" total capacity",16,accent,true);gap(box,18);
        JTextArea description=new JTextArea(p.description,6,48);description.setLineWrap(true);description.setWrapStyleWord(true);description.setEditable(false);description.setFont(new Font("SansSerif",Font.PLAIN,14));description.setBackground(soft);description.setForeground(text);description.setBorder(new EmptyBorder(14,14,14,14));JScrollPane scroll=new JScrollPane(description);scroll.setAlignmentX(LEFT_ALIGNMENT);box.add(scroll);gap(box,20);JPanel actions=flow(panel);
        if(service.user().get("role").equals("teacher")){actions.add(button("Edit project",()->{d.dispose();projectDialog(p);},true));actions.add(button("Delete",()->{if(JOptionPane.showConfirmDialog(d,"Delete this project and release assigned students?",Config.NAME,JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){service.deleteProject(p.id);d.dispose();render();}},false));}
        else{actions.add(button("Select / switch",()->{service.select(p.id);d.dispose();render();},true));actions.add(button(service.waiting(p.id)?"Leave waitlist":"Join waitlist",()->{service.waitlist(p.id);d.dispose();render();},false));}
        actions.add(button(service.saved(p.id)?"Unsave":"Save topic",()->{service.favorite(p.id);d.dispose();render();},false));actions.add(button("Close",d::dispose,false));actions.setAlignmentX(LEFT_ALIGNMENT);box.add(actions);d.pack();d.setMinimumSize(new Dimension(680,460));d.setSize(Math.max(680,d.getWidth()),Math.max(460,d.getHeight()));d.setLocationRelativeTo(this);d.setVisible(true);
    }
    void projectDialog(Project p){
        String[][] fields={{"title","Project title",p==null?"":p.title},{"supervisor","Supervisor",p==null?service.user().get("name"):p.supervisor},{"category","Category",p==null?"Software Engineering":p.category},{"description","Description",p==null?"":p.description},{"capacity","Total seats",p==null?"3":""+p.capacity},{"deadline","Deadline (YYYY-MM-DD)",p==null?LocalDate.now().plusDays(30).toString():p.deadline},{"status","Status",p==null?"Open":p.status}};
        formDialog(p==null?"Create a new project":"Edit project",fields,Map.of("status",new String[]{"Draft","Open","Closed"}),v->{Project next=new Project();next.title=v.get("title");next.supervisor=v.get("supervisor");next.category=v.get("category");next.description=v.get("description");try{next.capacity=Integer.parseInt(v.get("capacity"));}catch(NumberFormatException ex){throw new IllegalArgumentException("Capacity must be a whole number.");}next.deadline=v.get("deadline");next.status=v.get("status");service.saveProject(next,p==null?null:p.id);});
    }
    void studentsPage(){
        content.add(label("A live view of students and their current assignments.",13,muted,false),BorderLayout.NORTH);DefaultTableModel m=model(new String[]{"ACCOUNT ID","STUDENT","MAJOR","ASSIGNED PROJECT"});int assigned=0;
        for(Map<String,Object> r:service.students()){m.addRow(new Object[]{r.get("id"),r.get("name"),r.get("major"),r.get("project")});if(r.get("project_id")!=null)assigned++;}
        content.add(tablePanel(new JTable(m)));JPanel bottom=flow(bg);bottom.add(label(service.students().size()+" students • "+assigned+" assigned",13,muted,false));bottom.add(button("Export workspace JSON",()->export("json"),true));content.add(bottom,BorderLayout.SOUTH);
    }
    void selectionPage(){
        JPanel all=vertical(bg);Integer id=service.selected();if(id==null){addLabel(all,"Your next project is waiting. Explore the catalog to get started.",15,muted,false);gap(all,20);all.add(button("Explore projects",()->navigate("Projects"),true));}
        else{Project p=service.projects().stream().filter(v->v.id==id).findFirst().orElseThrow();JPanel card=card();addLabel(card,"YOUR CURRENT PROJECT",11,mint,true);gap(card,18);addLabel(card,p.title,27,text,true);gap(card,15);addLabel(card,p.supervisor+" / "+p.category,14,muted,false);gap(card,20);JTextArea desc=new JTextArea(p.description,4,48);desc.setLineWrap(true);desc.setWrapStyleWord(true);desc.setEditable(false);desc.setForeground(text);desc.setBackground(panel);desc.setFont(new Font("SansSerif",Font.PLAIN,14));desc.setAlignmentX(LEFT_ALIGNMENT);card.add(desc);gap(card,18);card.add(button("View project details",()->details(p),true));gap(card,12);card.add(button("Cancel selection",()->{if(JOptionPane.showConfirmDialog(this,"Cancel selection? A waiting student may receive this seat.",Config.NAME,JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){service.cancel();render();}},false));all.add(card);}
        gap(all,28);addLabel(all,"WAITLISTS",12,accent,true);gap(all,12);boolean any=false;for(Project p:service.projects())if(service.waiting(p.id)){addLabel(all,p.title,14,muted,false);any=true;}if(!any)addLabel(all,"You are not on a waitlist.",14,muted,false);content.add(all,BorderLayout.NORTH);
    }
    void activityPage(){content.add(label("Changes, selections, and waitlist promotions in one timeline.",13,muted,false),BorderLayout.NORTH);DefaultTableModel m=model(new String[]{"WHEN","ACCOUNT","ACTIVITY"});for(Models.Event e:service.activity())m.addRow(new Object[]{e.created.replace('T',' '),e.actor,e.message});JTable table=new JTable(m);table.getColumnModel().getColumn(2).setPreferredWidth(520);content.add(tablePanel(table));}
    void export(String kind){JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new File(Config.NAME+"-export."+kind));if(chooser.showSaveDialog(this)==JFileChooser.APPROVE_OPTION){try{service.export(chooser.getSelectedFile().toPath(),kind);JOptionPane.showMessageDialog(this,"Export saved.");}catch(IOException ex){throw new IllegalStateException(ex);}}}
    void settingsPage(){
        JPanel all=vertical(bg);JPanel card=card();addLabel(card,Config.NAME+" 2.0.0",26,text,true);gap(card,14);addLabel(card,Config.EDITION+" edition • Student project selection and supervision",14,muted,false);gap(card,15);addLabel(card,Config.AUTHOR.isEmpty()?"Personal edition":"Developer: "+Config.AUTHOR,13,text,false);
        if(!Config.GITHUB.isEmpty()){gap(card,20);card.add(button("Open developer GitHub",()->{try{Desktop.getDesktop().browse(java.net.URI.create(Config.GITHUB));}catch(Exception ex){throw new IllegalArgumentException("Open this URL in your browser: "+Config.GITHUB);}},false));}all.add(card);gap(all,25);
        JPanel actions=flow(bg);actions.setAlignmentX(LEFT_ALIGNMENT);actions.add(button("Change password",()->formDialog("Change password",new String[][]{{"old","Current password","","secret"},{"next","New password","","secret"}},Map.of(),v->service.changePassword(v.get("old"),v.get("next"))),true));actions.add(button("Toggle theme",this::toggleTheme,false));actions.add(button("Export JSON",()->export("json"),false));
        if(service.user().get("role").equals("teacher"))actions.add(button("Back up workspace",()->{JFileChooser ch=new JFileChooser();ch.setSelectedFile(new File(Config.NAME+"-backup.json"));if(ch.showSaveDialog(this)==JFileChooser.APPROVE_OPTION)try{service.backup(ch.getSelectedFile().toPath());JOptionPane.showMessageDialog(this,"Backup saved.");}catch(IOException ex){throw new IllegalStateException(ex);}},false));all.add(actions);gap(all,25);addLabel(all,"Keyboard shortcuts: Ctrl+F searches projects. F5 refreshes your view.",13,muted,false);gap(all,12);addLabel(all,"Data stays on this computer. Exports never include password hashes.",13,muted,false);gap(all,12);addLabel(all,"For restore instructions and original project history, see README.md.",13,muted,false);content.add(all,BorderLayout.NORTH);
    }
}
