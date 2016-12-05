package mymp3;

public class MYmp3View {

//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//
//	}
	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt){
		this.searchMp3();
	}
	private void searchMp3{
		search = new Thread(new SearchMp3());
	}
	
	private class SearchMp3 implements Runnable{
		private int read;
		private int total;
		Runnable updateBefore = new Runnable(){
		
			public void run(){
				progressBar.setVisible(true);
				progressBar.setIndeterminate(true);
				progressBar.setStringPainted(false);
				
			}
		};
		
		Runnable beforeProcess = new Runnable(){
			
			public void run(){
				progressBar.setIndeterminate(false);
				progressBar.setStringPainted(true);
				
			}
		};
		//���±����ʾ���߳�
//		Runnable update1 = new Runnable(){
//			public void run(){
//				jTable1.updateUI();
//				if(tota)
//			}
//		};
//	}
//
//}
/**********************************************************************************************/
		//���±����ʾ���߳�
Runnable update1 = new Runnable() {
    public void run() {
        jTable1.updateUI();
        if (total <= 0) {
            return;
        }
        progressBar.setValue(Integer.parseInt(
                String.valueOf(read * 100 / total)));
    }
};
Runnable searchOver = new Runnable() {
    public void run() {
        progressBar.setVisible(false);
    }
};
public void run() {
    try {
        //�������½��������߳�
        SwingUtilities.invokeLater(updateBefore);
        String keyword = URLEncoder.encode(jtf1.getText());
        //��ٶ��ύ���������URL
        String uStr = "http://mp3.baidu.com/m?f=ms&tn=baidump3&ct=134217728&lf=&rn=&word=" + keyword + "&lm=-1";
        //���ӷ�������ȡ�������
        String listPageCode = StringFilter.getHtmlCode(uStr);
        //������������н���
        //ȥ�����������ͷ��
        String[] temp = listPageCode.split("�����ٶ�[/r/n/t]*</th>[/r/n/t]*</tr>[/r/n/t]*<tr>");
        if (temp.length >= 2) { // tempС��2���ʾ�Ҳ�������
            //ȥ�����������β��
            temp = temp[1].split("</tr>[/r/n/t]*</table>");
            //���м������������зָ�
            temp = temp[0].split("</tr><tr>");//
            if (temp.length > 0) {
                total = temp.length;
                Mp3TableModel mtm = (Mp3TableModel) jTable1.getModel();
                mtm.clearValues();
                SwingUtilities.invokeLater(beforeProcess);
                for (String group : temp) {//����ÿһ��
                    read++;
                    MGroup mg = new MGroup(group);  // ��һ��ҳ������
                    String url = mg.getURL();
                    url = url.replaceAll(mg.getName(), URLEncoder.encode(mg.getName()));
                    //���������ȡ��һ�еĸ�������
String mp3PageCode = StringFilter.getHtmlCode(url);
                    //��ȡÿһ�׸��ʵ�ڵ�url
                    String mp3Url = getMp3Address(mp3PageCode);
                    Mp3Model mp3 = new Mp3Model(mg.getName(), mp3Url, mg.getSize());
                    mtm.addValue(mp3);
                    //�����̸߳��±��
                    SwingUtilities.invokeLater(update1);
                    if (read >= 20) {
                        break;
                    }
                }
            }
        }
        //��ɺ���½�����
        SwingUtilities.invokeLater(searchOver);
    } catch (Exception e) {
        //System.out.println("Exception e");
    }
}
}
/****************************************************************/
	  private String getMp3Address(String htmlCode) {
	        MP3URLParser parser = new MP3URLParser();
	        parser.parseVars(htmlCode);
	        htmlCode = parser.parse();
	        System.out.println(htmlCode);
	        return htmlCode;
	    }
	  /****************************************************************/
	  private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                         
	        textOutput.append("�����������/n");
	        addTasks();
	        jTable2.updateUI();
	        
	    }  
	  /****************************************************************/
	  private void addTasks() {
	        ArrayList<TaskModel> mp3ToLoad = new ArrayList<TaskModel>();
	        TableModel tableModel = jTable1.getModel();
	        int[] keys = jTable1.getSelectedRows();
	        if (tableModel instanceof Mp3TableModel) {
	            Mp3TableModel mtm = (Mp3TableModel) tableModel;
	            List<Mp3Model> mp3s = mtm.getValues();
	            for (int key : keys) {
	                Mp3Model mp3 = mp3s.get(key);
	                TaskModel tm = new TaskModel(mp3.getName().trim()+ AppConfig.getInstance().getCounter().increment(), mp3.getUrl());
	                mp3ToLoad.add(tm);
	                textOutput.append(tm+"/n");
	            }
	        }
	        if (manager == null) {
	            manager = new Manager(jTable2);
	            TableColumnModel tcm = jTable2.getColumnModel();
	            TableColumn tc = tcm.getColumn(TaskModel.COLUMN_PROCESS);
	            tc.setCellRenderer(new ProgressBarRenderer());
	        }
	        if (!mp3ToLoad.isEmpty()) {
	            manager.addTasks(mp3ToLoad);
	        }
	    }
	  /******************************************************/
	  public TaskModel(String name, String url) {
	        this.name = name;
	        this.url = url;
	        // �����������У�ֱ�Ӵ�����������
	        this.downloader = new Downloader(this); // �����������
	    }
	  /******************************************************/
	  public Downloader(TaskModel taskModel) {
	        textOutput = new TextOutput();
	        //���س���ִ�е�����
	        this.task = taskModel;
	        //����һ���첽�߳̽����ļ���С��ʼ��
	        Thread tt = new Thread(new Init());
	        tt.start();
	    }
	    private class Init implements Runnable {
	        public void run() {
	            init();
	        }
	    }
	    /**��ȡ�ļ���С
	     */
	    private void init() {
	        try {
	            if (totalBytes <= 0) {
	                URL u = new URL(task.getUrl());
	                totalBytes = u.openConnection().getContentLength();
	            }
	        } catch (Exception ex) {
	            System.out.println("Exception:" + this.getClass().getName());
	        }
	    }
	    /**************************************************************/
	    AppConfig.getInstance().getCounter().increment()
	    /**********************************************************/
	    private int MAX_ACTIVE_TASK = 3;
	    private Semaphore semaphore;
	    private AtomicCounter counter;    
	private AppConfig() {
	        semaphore = new Semaphore(MAX_ACTIVE_TASK);
	        counter = new AtomicCounter();
	    }
	/**********************************************************/
	  private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {                                         
	        // ��ʼ��ť
	        TableModel tm = jTable2.getModel();
	        int[] keys = jTable2.getSelectedRows();
	        if (keys.length > 0 && (tm instanceof TaskTableModel)) {
	            TaskTableModel ttm = (TaskTableModel) tm;
	            for (int key : keys) {
	                try {
	                    TaskModel task = ttm.getValue(key);
	                    task.toStart();
	                } catch (Exception exception) {
	                }
	            }
	        }
	    }             
		/**********************************************************/
	  public void toStart() {
	        downloader.toStart();
	    }
	  /**********************************************************/
	  public synchronized void toStart() {
	        if (isStopped()) {
	            this.start();
	            System.out.println("��ʼ����");
	        } else if (isPaused()) {
	            System.out.println("��������");
	            this.notifyAll();
	        }
	        this.state = STATE_LOADING;
	    }
	  /**********************************************************/
	  public void run() {
	        textOutput.appendln("׼����ʼ����:" + this.task);
	        try {
	            AppConfig.getInstance().getSemaphore().acquire();
	            textOutput.appendln("��ʼ����:" + this.task);
	        } catch (InterruptedException ex) {
	           ����
	        }
	����.
	/*******************************************************/
	AppConfig.getInstance().getSemaphore().release();
	/*******************************************************/
	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // ��ͣ��ť
        TableModel tm = jTable2.getModel();
        int[] keys = jTable2.getSelectedRows();
        if (tm instanceof TaskTableModel) {
            TaskTableModel ttm = (TaskTableModel) tm;
            for (int key : keys) {
                try {
                    TaskModel task = ttm.getValue(key);
                    task.toPause();
                } catch (Exception exception) {
                }
            }
        }
    }        
	/***********************************************************/
	public synchronized void toPause() {
        this.state = STATE_PAUSED;
    }
	/**************************************************/
	synchronized (this) {
        if (isPaused()) {
            try {
                this.wait();
            } catch (InterruptedException ie) {
                //  AppConfig.getInstance().getSemaphore().release();
                System.out.println("InterruptedException");
            }
        }
    }
	/**********************************************/
	public void toStop() {
        downloader.stopDownload();
    }
	/**********************************************/
	 /** ͣ���������� */
    public void stopDownload() {
        this.state = STATE_STOPPED;
        this.interrupt();
        System.out.println("��ֹ");
    }
    /*********************************************/
    //ÿѭ��һ�Σ�����Ƿ��жϣ�����жϣ���ֹͣ
    if (Thread.interrupted()) {
        toStop();
        in.close();
        out.close();
        AppConfig.getInstance().getSemaphore().release();
        return;
    }
}
	  /*********************************************/
	  private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {                                         
	        removeTask();
	        jTable2.updateUI();
	    }   
	  /*****************************************/
	  public void removeTasks(List<TaskModel> tasks) {
	        //ɾ����ʱ��ע��ͬ��
	        for (TaskModel tm : tasks) {
	            if (!tm.isOk()) {   // �������δ���,��ֹͣ
	                tm.toStop();
	            }
	            //ר����ʾ�������ݽṹ��
	          //  freeLockValues.remove(tm);
	        }
	        synchronized (this) {
	            values.removeAll(tasks);
	        }
	}
	  /**********************************************/
	  private class CheckOKTask extends TimerTask {
	        @Override
	        public void run() {
	            // ��鲢�Ƴ��Ѿ���ɵ�����
	            Lock lock = new ReentrantLock(false);
	            try {
	                TableModel tm = jTable.getModel();
	                if (tm instanceof TaskTableModel) {
	                    TaskTableModel ttm = (TaskTableModel) tm;
	                    List<TaskModel> tasks = ttm.getValues();
	                    if (null != tasks && !tasks.isEmpty()) {
	                        lock.lock();
	                        synchronized (tasks) {
	                            Iterator it = tasks.iterator();
	                            while (it.hasNext()) {
	                                TaskModel temp = (TaskModel) it.next();
	                                if (temp.isOk()) {
	                                    //ɾ����ʱ��ע��ͬ��
	                                    ttm.getFreeLockValues().add(temp);
	                                    it.remove();
	                                    textOutput.appendln("ɾ���������:" + temp);
	                                }
	                            }
	                        }
	                        if (tasks.isEmpty()) {
	                            textOutput.appendln("�����Ѿ���ɵ�������:");
	                            for (TaskModel tm2 : ttm.getFreeLockValues()) {
	                                textOutput.appendln(tm2.toString());
	                            }
	                        }
	                    }
	                }
	                jTable.updateUI();
	            } catch (Exception exception) {
	            }
	        }
	    }
	}
/***************************************************************/
public class AtomicCounter {
    private static AtomicInteger value = new AtomicInteger();
    public int getValue() {
        return value.get();
    }
    public int increment() {
        return value.incrementAndGet();
    }
    public int increment(int i) {
        return value.addAndGet(i);
    }
    public int decrement() {
        return value.decrementAndGet();
    }
    public int decrement(int i) {
        return value.addAndGet(-i);
    }
}
/********************************************************/
public class TaskTableModel extends AbstractTableModel {
    private List<TaskModel> values = new ArrayList<TaskModel>();
    private List<TaskModel>  freeLockValues=new LockFreeList<TaskModel>();
����
/****************************************/
if (temp.isOk()) {
    //ɾ����ʱ��ע��ͬ��
 ttm.getFreeLockValues().add(temp);
 it.remove();
 ����
}