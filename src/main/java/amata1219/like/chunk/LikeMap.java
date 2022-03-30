package amata1219.like.chunk;

import java.util.stream.Collectors;

import amata1219.like.consts.Like;

public class LikeMap extends ChunkMap<Like> {
	
	public void put(Like like){
		put(like.blockX(), like.blockZ(), like);
	}
	
	public void remove(Like like){
		remove(like.blockX(), like.blockZ(), like);
	}
	
	public boolean contains(Like like){
		return get(like.blockX(), like.blockZ()).contains(like);
	}
	
	@Override
	public String toString(){
		return values().stream()
				.map(l -> l.id)
				.map(String::valueOf)
				.collect(Collectors.joining(","));
	}
	
}
