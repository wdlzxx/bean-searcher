package com.ejlchina.searcher.bean;

import java.util.Date;

import com.ejlchina.searcher.param.Operator;

@SearchBean(tables = "user")
public class User {
	
	@DbField("id")
	private Long id; 
	
	@DbField(value = "name", onlyOn = Operator.StartWith)
	private String name;
	
	@DbField("age")
	private int age;

	@DbField(value = "date_created", conditional = false)
	private Date dateCreated;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
}
