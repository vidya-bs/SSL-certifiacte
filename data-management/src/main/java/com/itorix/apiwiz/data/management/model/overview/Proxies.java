package com.itorix.apiwiz.data.management.model.overview;

import java.util.List;

public class Proxies {
		private List<String> targetservers;

		private String revision;

		private List<String> cache;

		private String name;

		private List<Products> products;

		private List<String> kvm;

		public List<String> getTargetservers() {
			return targetservers;
		}

		public void setTargetservers(List<String> targetservers) {
			this.targetservers = targetservers;
		}

		public String getRevision() {
			return revision;
		}

		public void setRevision(String revision) {
			this.revision = revision;
		}

		public List<String> getCache() {
			return cache;
		}

		public void setCache(List<String> cache) {
			this.cache = cache;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<Products> getProducts() {
			return products;
		}

		public void setProducts(List<Products> products) {
			this.products = products;
		}

		public List<String> getKvm() {
			return kvm;
		}

		public void setKvm(List<String> kvm) {
			this.kvm = kvm;
		}

		@Override
		public String toString() {
			return "ClassPojo [targetservers = " + targetservers + ", revision = " + revision + ", cache = " + cache
					+ ", name = " + name + ", products = " + products + ", kvm = " + kvm + "]";
		}

		
	}