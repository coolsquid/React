package coolsquid.react.integration;

import java.util.List;

import com.google.common.collect.Lists;

public interface Integration {

	public static final List<Integration> INTEGRATIONS = Lists.newArrayList(new FTBLibIntegration());

	void enable();

	void disable();

	String getModID();
}