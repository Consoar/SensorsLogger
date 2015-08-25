package bos.whu.sensorslogger.support;

import java.util.ArrayList;

public class LogEntity {
	public long timestamp;
	public int type;
	public ArrayList<Float> data;
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public ArrayList<Float> getData() {
		return data;
	}
	public void setData(ArrayList<Float> data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "LogEntity [timestamp=" + timestamp + ", type=" + type
				+ ", data=" + data + "]";
	}
	
}
