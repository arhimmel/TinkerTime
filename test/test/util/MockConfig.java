package test.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import aohara.tinkertime.config.Config;

public class MockConfig extends Config {
	
	@Override
	public Path getGameDataPath(){
		return Paths.get("/");
	}
	
	@Override
	public Path getModsPath(){
		return Paths.get("/");
	}
}