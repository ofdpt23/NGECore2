/*******************************************************************************
 * Copyright (c) 2013 <Project SWG>
 * 
 * This File is part of NGECore2.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Using NGEngine to work with NGECore2 is making a combined work based on NGEngine. 
 * Therefore all terms and conditions of the GNU Lesser General Public License cover the combination.
 ******************************************************************************/
package resources.z.exp.objects;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.mina.core.buffer.IoBuffer;

import resources.objects.IDelta;
import resources.z.exp.objects.object.BaseObject;

import com.sleepycat.persist.model.NotPersistent;
import com.sleepycat.persist.model.Persistent;

/* A SWGList element should extend Delta or implement IDelta */

@Persistent
public class SWGList<E> implements List<E> {
	
	private List<E> list = new CopyOnWriteArrayList<E>();
	@NotPersistent
	private int updateCounter = 0;
	private BaseObject object;
	private byte viewType;
	private short updateType;
	private boolean addByte;
	@NotPersistent
	protected final Object objectMutex = new Object();
	
	public SWGList() { }
	
	public SWGList(BaseObject object, int viewType, int updateType, boolean addByte) {
		this.object = object;
		this.viewType = (byte) viewType;
		this.updateType = (short) updateType;
		this.addByte = addByte;
	}
	
	public boolean add(E e) {
		synchronized(objectMutex) {				
			if (valid(e) && list.add(e)) {
					queue(item(1, list.lastIndexOf(e), Baseline.toBytes(e), true, true));
					return true;
			}
			
			return false;
		}
	}
	
	public void add(int index, E element) {
		synchronized(objectMutex) {
			if (valid(element)) {
				list.add(index, element);
				queue(item(1, index, Baseline.toBytes(element), true, true));
			}
		}
	}
	
	public boolean addAll(Collection<? extends E> c) {
		synchronized(objectMutex) {
			if (!c.isEmpty()) {
				List<byte[]> buffer = new ArrayList<byte[]>();
				boolean success = false;
				
				for (E element : c) {
					if (valid(element)) {
						if (list.add(element)) {
							buffer.add(item(1, list.lastIndexOf(element), Baseline.toBytes(element), true, true));
							success = true;
						}
					} else {
						return false;
					}
				}
				
				if (success == true) {
					queue(buffer);
				} else {
					return false;
				}
			}
			
			return false;
		}
	}
	
	public boolean addAll(int index, Collection<? extends E> c) {
		synchronized(objectMutex) {
			if (!c.isEmpty()) {
				List<byte[]> buffer = new ArrayList<byte[]>();
				
				for (E element : c) {
					if (valid(element)) {
						list.add(index, element);
						buffer.add(item(1, index, Baseline.toBytes(element), true, true));
						index++;
					} else {
						return false;
					}
				}
				
				queue(buffer);
				
				return true;
			}
			
			return false;
		}
	}
	
	public void clear() {
		synchronized(objectMutex) {
			list.clear();
			queue(item(4, 0, null, false, false));
		}
	}
	
	public boolean contains(Object o) {
		synchronized(objectMutex) {
			return list.contains(o);
		}
	}
	
	public boolean containsAll(Collection<?> c) {
		synchronized(objectMutex) {
			return list.containsAll(c);
		}
	}
	
	public E get(int index) {
		synchronized(objectMutex) {
			return list.get(index);
		}
	}
	
	public List<E> get() {
		return list;
	}
	
	public int indexOf(Object o) {
		synchronized(objectMutex) {
			return list.indexOf(o);
		}
	}
	
	public boolean isEmpty() {
		synchronized(objectMutex) {
			return list.isEmpty();
		}
	}
	
	public Iterator<E> iterator() {
		synchronized(objectMutex) {
			return list.iterator();
		}
	}
	
	public int lastIndexOf(Object o) {
		synchronized(objectMutex) {
			return list.lastIndexOf(o);
		}
	}
	
	public ListIterator<E> listIterator() {
		synchronized(objectMutex) {
			return list.listIterator();
		}
	}
	
	public ListIterator<E> listIterator(int index) {
		synchronized(objectMutex) {
			return listIterator(index);
		}
	}
	
	public boolean remove(Object o) {
		synchronized(objectMutex) {
			int index = list.indexOf(o);
			
			if (list.remove(o)) {
				queue(item(1, index, null, true, false));
				return true;
			} else {
				return false;
			}
		}
	}
	
	public E remove(int index) {
		synchronized(objectMutex) {
			E element = list.remove(index);
			
			queue(item(1, index, null, true, false));
			
			return (E) element;
		}
	}
	
	public boolean removeAll(Collection<?> c) {
		synchronized(objectMutex) {
			if (!c.isEmpty()) {
				List<byte[]> buffer = new ArrayList<byte[]>();
				int index;
				boolean success = false;
				
				for (Object element : c) {
					index = list.indexOf(element);
					
					if (list.remove(element)) {
						buffer.add(item(0, index, null, true, false));
						success = true;
					}
				}
				
				if (success) {
					queue(buffer);
				}
				
				return success;
			}
			
			return false;
		}
	}
	
	public boolean retainAll(Collection<?> c) {
		synchronized(objectMutex) {
			return list.retainAll(c);
		}
	}
	
	public E set(int index, E element) {
		synchronized(objectMutex) {
			if (valid(element)) {
				E previousElement = list.set(index, element);
				
				queue(item(2, index, Baseline.toBytes(element), true, true));
				
				return previousElement;
			}
			
			return null;
		}
	}
	
	public boolean set(List<E> list) {
		synchronized(objectMutex) {
			byte[] newListData = { 0x03 };
			
			if (!list.isEmpty()) {
				for (E element : list) {
					if (valid(element)) {
						IoBuffer buffer = IoBuffer.allocate((newListData.length + Baseline.toBytes(element).length), false).order(ByteOrder.LITTLE_ENDIAN);
						buffer.put(newListData);
						buffer.put(Baseline.toBytes(element));
						newListData = buffer.array();
					} else {
						return false;
					}
				}
				
				this.list = list;
				
				updateCounter++;
				queue(newListData);
					
				return true;
			}
			
			return false;
		}
	}
	
	public int size() {
		synchronized(objectMutex) {
			return list.size();
		}
	}
	
	public List<E> subList(int fromIndex, int toIndex) {
		synchronized(objectMutex) {
			return list.subList(fromIndex, toIndex);
		}
	}
	
	public Object[] toArray() {
		synchronized(objectMutex) {
			return list.toArray();
		}
	}
	
	public <T> T[] toArray(T[] a) {
		synchronized(objectMutex) {
			return list.toArray(a);
		}
	}
	
	public int getUpdateCounter() {
		synchronized(objectMutex) {
			return updateCounter;
		}
	}
	
	public Object getMutex() {
		return objectMutex;
	}
	
	public byte[] getBytes() {
		synchronized(objectMutex) {
			byte[] objects = { };
			int size = 0;
			
			for (Object o : list) {
				byte[] object = Baseline.toBytes(o);
				size += object.length;
				
				IoBuffer buffer = Baseline.createBuffer(size);
				buffer.put(objects);
				if (addByte) buffer.put((byte) 0);
				buffer.put(object);
				buffer.flip();
				
				objects = buffer.array();
			}
			
			IoBuffer buffer = Baseline.createBuffer(8 + size);
			buffer.putInt(list.size());
			buffer.putInt(updateCounter);
			buffer.put(objects);
			buffer.flip();
			
			return buffer.array();
		}
	}
	
	private boolean valid(Object o) {
		if (o instanceof String || o instanceof Byte || o instanceof Short ||
		o instanceof Integer || o instanceof Float || o instanceof Long ||
		o instanceof IDelta) {
			return true;
		} else {
			return false;
		}
	}
	
	private byte[] item(int type, int index, byte[] data, boolean useIndex, boolean useData) {
		int size = 1 + ((useIndex) ? 2 : 0) + ((useData) ? data.length : 0);
			
		IoBuffer buffer = IoBuffer.allocate((size), false).order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) type);
		if (useIndex) buffer.putShort((short) index);
		if (useData) buffer.put(data);
		buffer.flip();
			
		updateCounter++;
			
		return buffer.array();
	}
	
	private void queue(byte[] data) {
		IoBuffer buffer = IoBuffer.allocate((data.length + 8), false).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(1);
		buffer.putInt(updateCounter);
		buffer.put(data);
		buffer.flip();
		object.sendListDelta(viewType, updateType, buffer);
	}
	
	private void queue(List<byte[]> data) {
		int size = 0;
		
		for (byte[] queued : data) {
			size += queued.length;
		}
		
		IoBuffer buffer = IoBuffer.allocate((size + 8), false).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(data.size());
		buffer.putInt(updateCounter);
		for (byte[] queued : data) buffer.put(queued);
		buffer.flip();
		
		object.sendListDelta(viewType, updateType, buffer);
	}
	
}
