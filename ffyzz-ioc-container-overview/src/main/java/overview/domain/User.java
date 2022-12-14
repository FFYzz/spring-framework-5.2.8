package overview.domain;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.io.Resource;
import overview.enums.City;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @Title:
 * @Author: FFYzz
 * @Mail: cryptochen95 at gmail dot com
 * @Date: 2020/10/6
 */
public class User implements BeanNameAware {

	private Long id;

	private String name;

	private City city;

	private Resource configFileLocation;

	private City[] workCities;

	private List<City> liveCities;

	private transient String beanName;

	private Company company;

	private Properties context;

	private String contextText;

	public static User createUser() {
		User user = new User();
		user.setName("static user");
		user.setId(20L);
		return user;
	}

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

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	public Resource getConfigFileLocation() {
		return configFileLocation;
	}

	public void setConfigFileLocation(Resource configFileLocation) {
		this.configFileLocation = configFileLocation;
	}

	public City[] getWorkCities() {
		return workCities;
	}

	public void setWorkCities(City[] workCities) {
		this.workCities = workCities;
	}

	public List<City> getLiveCities() {
		return liveCities;
	}

	public void setLiveCities(List<City> liveCities) {
		this.liveCities = liveCities;
	}

	public Company getCompany() {
		return company;
	}

	public void setCompany(Company company) {
		this.company = company;
	}

	public Properties getContext() {
		return context;
	}

	public void setContext(Properties context) {
		this.context = context;
	}

	public String getContextText() {
		return contextText;
	}

	public void setContextText(String contextText) {
		this.contextText = contextText;
	}

	public void initMethodByJavaAPI() {
		name = name + " initMethodByJavaAPI";
	}

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", city=" + city +
				", configFileLocation=" + configFileLocation +
				", workCities=" + Arrays.toString(workCities) +
				", liveCities=" + liveCities +
				", beanName='" + beanName + '\'' +
				", company=" + company +
				", context=" + context +
				", contextText='" + contextText + '\'' +
				'}';
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name + " " + id;
	}

	//    @PostConstruct
	public void init() {
		System.out.println("User Bean [" + beanName + "] ?????????...");
	}

	//    @PreDestroy
	public void destroy() {
		System.out.println("User Bean [" + beanName + "] ?????????...");
	}

}
