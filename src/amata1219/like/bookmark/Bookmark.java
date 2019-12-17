package amata1219.like.bookmark;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import amata1219.like.Like;
import amata1219.like.bookmark.Order;

public class Bookmark {
	
	public final String name;
	private final List<Like> likes;
	private Order order;
	
	public Bookmark(String name, List<Like> likes, Order order){
		this.name = name;
		this.likes = likes;
		this.order = order;
	}
	
	public Bookmark(String name){
		this(name, new ArrayList<>(), Order.REGISTRATION_TIME_IN_DESCENDING);
	}
	
	public List<Like> likes(){
		return new ArrayList<>(likes);
	}
	
	public Order order(){
		return order;
	}
	
	public void setOrder(Order order){
		this.order = Objects.requireNonNull(order);
	}
	
}
