package hhup.repository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;

public class Repository<T> implements Set<T> {

	protected Logger log = LoggerFactory.getLogger(this.getClass());

	private ObjectMapper mapper;
	private Set<T> items;
	private Class<T[]> entityArrayClass;
	private String jsonPath;

	public Repository(Class<T[]> entityArrayClass, String jsonPath) {
		this.entityArrayClass = entityArrayClass;
		this.jsonPath = jsonPath;
		mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());
		reload();
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public boolean isEmpty() {
		return items.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return items.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return items.iterator();
	}

	@Override
	public Object[] toArray() {
		return items.toArray();
	}

	@Override
	public <X> X[] toArray(X[] a) {
		return items.toArray(a);
	}

	@Override
	public boolean add(T e) {
		if (items.contains(e)) { return false; }
		ImmutableSet<T> newItems = ImmutableSet.<T> builder().addAll(items).add(e).build();
		synchronized (items) {
			items = newItems;
			save();
		}
		return true;
	}

	@Override
	public boolean remove(Object o) {
		boolean removed = false;
		Builder<T> builder = ImmutableSet.<T> builder();
		for (T item : items) {
			if (!item.equals(o)) {
				builder.add(item);
			} else {
				removed = true;
			}
		}
		ImmutableSet<T> newItems = builder.build();
		synchronized (items) {
			items = newItems;
			save();
		}
		return removed;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return items.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (items.containsAll(c)) { return false; }
		ImmutableSet<T> newItems = ImmutableSet.<T> builder().addAll(items).addAll(c).build();
		synchronized (items) {
			items = newItems;
			save();
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean changed = false;
		Builder<T> builder = ImmutableSet.<T> builder();
		for (T item : items) {
			if (c.contains(item)) {
				builder.add(item);
			} else {
				changed = true;
			}
		}
		ImmutableSet<T> newItems = builder.build();
		synchronized (items) {
			items = newItems;
			save();
		}
		return changed;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean changed = false;
		Builder<T> builder = ImmutableSet.<T> builder();
		for (T item : items) {
			if (!c.contains(item)) {
				builder.add(item);
			} else {
				changed = true;
			}
		}
		ImmutableSet<T> newItems = builder.build();
		synchronized (items) {
			items = newItems;
			save();
		}
		return changed;
	}

	@Override
	public void clear() {
		synchronized (items) {
			items = ImmutableSet.of();
			save();
		}
	}

	public void reload() {
		items = ImmutableSet.<T> copyOf(load());
	}

	public void retainAll(Predicate<T> predicate) {
		synchronized (items) {
			items = Sets.filter(items, predicate);
			save();
		}
	}

	public void removeAll(Predicate<T> predicate) {
		Predicate<T> inverted = Predicates.not(predicate);
		retainAll(inverted);
	}

	public T findFirst(Predicate<T> predicate) {
		for (T item : items) {
			if (predicate.apply(item)) { return item; }
		}
		return null;
	}

	public Set<T> findAll(Predicate<T> predicate) {
		return ImmutableSet.copyOf(Sets.filter(items, predicate));
	}

	public boolean removeFirst(Predicate<T> predicate) {
		T item = findFirst(predicate);
		if (item != null) {
			remove(item);
			return true;
		}
		return false;
	}

	public boolean replace(T item) {
		if (remove(item)) {
			add(item);
			return true;
		}
		return false;
	}

	public boolean alterFirstItem(Predicate<T> identifier, Function<T, T> alterer) {
		T item = findFirst(identifier);
		if (item == null) { return false; }
		T newItem = alterer.apply(item);
		return replace(newItem);
	}

	public boolean containsAny(Predicate<T> predicate) {
		return findFirst(predicate) != null;
	}

	private Collection<T> load() {
		try {
			File file = getFile();
			log.info("{}: reading '{}' ", this.getClass().getName(), file.getAbsolutePath());
			return Sets.newHashSet(mapper.readValue(file, entityArrayClass));
		} catch (Exception e) {
			log.warn(e.toString());
			return Collections.emptySet();
		}
	}

	private void save() {
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(getFile(), items);
		} catch (Exception e) {
			log.warn("cannot write to file '{}': {}", jsonPath, e);
		}
	}

	private File getFile() throws IOException {
		File file = new File(jsonPath);
		if (!file.exists()) {
			FileUtils.touch(file);
		}
		return file;
	}
}