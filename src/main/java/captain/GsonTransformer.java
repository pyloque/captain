package captain;

import org.json.JSONObject;

import spark.ResponseTransformer;

public class GsonTransformer implements ResponseTransformer {

	@Override
	public String render(Object model) {
		return JSONObject.valueToString(model);
	}
}
