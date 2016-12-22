#version 400 core

in vec2 pass_textureCoords;
in vec3 surfaceNormal;
in vec3 toLightVector;
in vec3 toCameraVector;
in float visibility;

out vec4 out_color;

uniform sampler2D textureSampler;
uniform vec3 lightColor;

uniform float shineDamper;
uniform float reflectivity;

uniform vec3 skyColor;

void main(void) {

    vec3 unitNormal = normalize(surfaceNormal);
    vec3 unitLightVector = normalize(toLightVector);

    float nDotl = dot(unitNormal, unitLightVector);
    float brightness = max(nDotl, 0.15);
    vec3 diffuse = brightness * lightColor;


    vec3 unitVectorToCamera = normalize(toCameraVector);
    vec3 lightDirection = -unitVectorToCamera;
    vec3 reflectLightDirection = reflect(lightDirection, unitNormal);

    float specularFactor = dot(reflectLightDirection, unitVectorToCamera);
    specularFactor = max(specularFactor, 0.0);
    float dampedFactor = pow(specularFactor, shineDamper);
    vec3 finalSpecular = dampedFactor * lightColor;

    out_color = vec4(diffuse, 1.0) * texture(textureSampler, pass_textureCoords) + vec4(finalSpecular, 1.0);
    out_color = mix(vec4(skyColor, 1.0), out_color, visibility);
}