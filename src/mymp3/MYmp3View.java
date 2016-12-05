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
		//更新表格显示的线程
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
		//更新表格显示的线程
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
        //启动更新进度条的线程
        SwingUtilities.invokeLater(updateBefore);
        String keyword = URLEncoder.encode(jtf1.getText());
        //向百度提交搜索请求的URL
        String uStr = "http://mp3.baidu.com/m?f=ms&tn=baidump3&ct=134217728&lf=&rn=&word=" + keyword + "&lm=-1";
        //连接服务器获取搜索结果
        String listPageCode = StringFilter.getHtmlCode(uStr);
        //对搜索结果进行解析
        //去除搜索结果的头部
        String[] temp = listPageCode.split("链接速度[/r/n/t]*</th>[/r/n/t]*</tr>[/r/n/t]*<tr>");
        if (temp.length >= 2) { // temp小于2则表示找不到数据
            //去除搜索结果的尾部
            temp = temp[1].split("</tr>[/r/n/t]*</table>");
            //把中间的搜索结果按行分割
            temp = temp[0].split("</tr><tr>");//
            if (temp.length > 0) {
                total = temp.length;
                Mp3TableModel mtm = (Mp3TableModel) jTable1.getModel();
                mtm.clearValues();
                SwingUtilities.invokeLater(beforeProcess);
                for (String group : temp) {//解析每一行
                    read++;
                    MGroup mg = new MGroup(group);  // 第一个页面数据
                    String url = mg.getURL();
                    url = url.replaceAll(mg.getName(), URLEncoder.encode(mg.getName()));
                    //访问网络获取这一行的歌曲数据
String mp3PageCode = StringFilter.getHtmlCode(url);
                    //获取每一首歌的实在的url
                    String mp3Url = getMp3Address(mp3PageCode);
                    Mp3Model mp3 = new Mp3Model(mg.getName(), mp3Url, mg.getSize());
                    mtm.addValue(mp3);
                    //调用线程更新表格
                    SwingUtilities.invokeLater(update1);
                    if (read >= 20) {
                        break;
                    }
                }
            }
        }
        //完成后更新进度条
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
	        textOutput.append("添加下载任务/n");
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
	        // 在下载任务中，直接创建了下载器
	        this.downloader = new Downloader(this); // 添加下载任务
	    }
	  /******************************************************/
	  public Downloader(TaskModel taskModel) {
	        textOutput = new TextOutput();
	        //下载程序执行的任务
	        this.task = taskModel;
	        //创建一个异步线程进行文件大小初始化
	        Thread tt = new Thread(new Init());
	        tt.start();
	    }
	    private class Init implements Runnable {
	        public void run() {
	            init();
	        }
	    }
	    /**获取文件大小
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
	        // 开始按钮
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
	            System.out.println("开始下载");
	        } else if (isPaused()) {
	            System.out.println("继续下载");
	            this.notifyAll();
	        }
	        this.state = STATE_LOADING;
	    }
	  /**********************************************************/
	  public void run() {
	        textOutput.appendln("准备开始下载:" + this.task);
	        try {
	            AppConfig.getInstance().getSemaphore().acquire();
	            textOutput.appendln("开始下载:" + this.task);
	        } catch (InterruptedException ex) {
	           ……
	        }
	…….
	/*******************************************************/
	AppConfig.getInstance().getSemaphore().release();
	/*******************************************************/
	private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {                                         
        // 暂停按钮
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
	 /** 停下下载任务 */
    public void stopDownload() {
        this.state = STATE_STOPPED;
        this.interrupt();
        System.out.println("终止");
    }
    /*********************************************/
    //每循环一次，检测是否被中断，如果中断，则停止
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
	        //删除的时候注意同步
	        for (TaskModel tm : tasks) {
	            if (!tm.isOk()) {   // 如果任务未完成,则停止
	                tm.toStop();
	            }
	            //专门演示无锁数据结构的
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
	            // 检查并移除已经完成的任务
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
	                                    //删除的时候注意同步
	                                    ttm.getFreeLockValues().add(temp);
	                                    it.remove();
	                                    textOutput.appendln("删除完成任务:" + temp);
	                                }
	                            }
	                        }
	                        if (tasks.isEmpty()) {
	                            textOutput.appendln("所有已经完成的任务是:");
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
……
/****************************************/
if (temp.isOk()) {
    //删除的时候注意同步
 ttm.getFreeLockValues().add(temp);
 it.remove();
 ……
}