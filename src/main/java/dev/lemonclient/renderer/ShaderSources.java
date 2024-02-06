package dev.lemonclient.renderer;

public class ShaderSources {


    public static final String POSITION_COLOR_TEX_SOURCE = """
        #version 330 core

        layout (location = 0) in vec4 pos;
        layout (location = 1) in vec2 uv0;
        layout (location = 2) in vec4 color;

        uniform mat4 u_Proj;
        uniform mat4 u_ModelView;

        out vec2 texCoord0;
        out vec4 vertexColor;

        void main() {
            gl_Position = u_Proj * u_ModelView * pos;

            texCoord0 = uv0;
            vertexColor = color;
        }
        """;

    public static final String POSITION_COLOR_SOURCE = """
        #version 330 core

        layout (location = 0) in vec4 pos;
        layout (location = 1) in vec4 color;

        uniform mat4 u_Proj;
        uniform mat4 u_ModelView;

        out vec4 vertexColor;

        void main() {
            gl_Position = u_Proj * u_ModelView * pos;

            vertexColor = color;
        }
        """;

    public static final String ROUND_RECT_SOURCE = """
        #version 330 core

        in vec4 vertexColor;
        uniform vec2 uSize;
        uniform vec2 uLocation;
        uniform float Size;

        out vec4 fragColor;

        float roundedBoxSDF(vec2 center, vec2 size, float radius) {
            return length(max(abs(center), 0.0)) - radius;
        }

        void main() {
            float distance = roundedBoxSDF(gl_FragCoord.xy - uLocation, uSize, Size);
            float smoothedAlpha = 1.0 - smoothstep(-1.0, 1.0, distance);
            fragColor = vec4(vertexColor.rgb, vertexColor.a * smoothedAlpha);
        }

        """;


    public static final String MAIN_MENU_SOURCE = """
        #version 330 core

        uniform vec2 uSize;
        uniform float Time;
        uniform int Type;

        out vec4 fragColor;

        float speed = .3;
        float scale = 2.2;
        int octaves = 4;
        bool turbulence = false;
        float shift = .3;
        float startAmp = .8;

        vec4 permute(vec4 x){return mod(((x*34.0)+1.0)*x, 289.0);}
        vec4 taylorInvSqrt(vec4 r){return 1.79284291400159 - 0.85373472095314 * r;}

        float snoise(vec3 v){
            const vec2  C = vec2(1.0/6.0, 1.0/3.0) ;
            const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);
            vec3 i  = floor(v + dot(v, C.yyy) );
            vec3 x0 =   v - i + dot(i, C.xxx) ;
            vec3 g = step(x0.yzx, x0.xyz);
            vec3 l = 1.0 - g;
            vec3 i1 = min( g.xyz, l.zxy );
            vec3 i2 = max( g.xyz, l.zxy );
            vec3 x1 = x0 - i1 + 1.0 * C.xxx;
            vec3 x2 = x0 - i2 + 2.0 * C.xxx;
            vec3 x3 = x0 - 1. + 3.0 * C.xxx;
            i = mod(i, 289.0 );
            vec4 p = permute( permute( permute(
            i.z + vec4(0.0, i1.z, i2.z, 1.0 ))
            + i.y + vec4(0.0, i1.y, i2.y, 1.0 ))
            + i.x + vec4(0.0, i1.x, i2.x, 1.0 ));
            float n_ = 1.0/7.0; // N=7
            vec3  ns = n_ * D.wyz - D.xzx;

            vec4 j = p - 49.0 * floor(p * ns.z *ns.z);

            vec4 x_ = floor(j * ns.z);
            vec4 y_ = floor(j - 7.0 * x_ );

            vec4 x = x_ *ns.x + ns.yyyy;
            vec4 y = y_ *ns.x + ns.yyyy;
            vec4 h = 1.0 - abs(x) - abs(y);

            vec4 b0 = vec4( x.xy, y.xy );
            vec4 b1 = vec4( x.zw, y.zw );

            vec4 s0 = floor(b0)*2.0 + 1.0;
            vec4 s1 = floor(b1)*2.0 + 1.0;
            vec4 sh = -step(h, vec4(0.0));

            vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy ;
            vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww ;

            vec3 p0 = vec3(a0.xy,h.x);
            vec3 p1 = vec3(a0.zw,h.y);
            vec3 p2 = vec3(a1.xy,h.z);
            vec3 p3 = vec3(a1.zw,h.w);
            vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2, p2), dot(p3,p3)));
            p0 *= norm.x;
            p1 *= norm.y;
            p2 *= norm.z;
            p3 *= norm.w;

            vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
            m = m * m;
            return 42.0 * dot( m*m, vec4( dot(p0,x0), dot(p1,x1),
            dot(p2,x2), dot(p3,x3) ) );
        }
        float fbm (in vec3 st) {
            // Initial values
            float value = 0.0;
            float amplitude = startAmp;
            float frequency = 0.;

            // Loop of octaves
            for (int i = 0; i < octaves; i++) {
                value += amplitude * snoise(st);
                st *= 2.;
                amplitude *= .5;
            }
            return value;
        }


        vec4 Effects(float speed, vec2 uv, float time)
        {
            float t = mod(time*speed,60.0);
            float rt =0.01*sin(t*0.45);
            mat2 m1 = mat2(cos(rt),-sin(rt),-sin(rt),cos(rt));
            vec2 uva=uv*m1;
            float irt = 0.005* cos(t*0.05);
            mat2 m2 = mat2(sin(irt),cos(irt),-cos(irt),sin(irt));
            for(int i=1;i<40;i+=1)
            {
                float it = float(i);
                uva*=(m2);
                uva.y+=-1.0+(0.6/it) * cos(t + it*uva.x + 0.5*it)
                *float(mod(it,2.0)==0.0);
                uva.x+=1.0+(0.5/it) * cos(t + it*uva.y + 0.5*(it+15.0));
            }
            //Intensity range from 0 to n;
            float n = 1.;
            float r = n + n * sin(4.0*uva.x+t) * 0.4;
            float gb = n + n * sin(3.0*uva.y);
            if(gb<1. && r<1.){
                float rg = 0.5;
                return vec4(r*0.2*rg , gb*0.1*r*rg , gb*r*0.5 , 1.);
            }
            return vec4(r*0.02 , gb*0.2*r , gb*r*0.4 , 1.);
        }
        const float MATH_PI	= float( 3.14159265359 );

        void Rotate( inout vec2 p, float a )
        {
            p = cos( a ) * p + sin( a ) * vec2( p.y, -p.x );
        }

        float Circle( vec2 p, float r )
        {
            return ( length( p / r ) - 1.0 ) * r;
        }

        float Rand( vec2 c )
        {
            return fract( sin( dot( c.xy, vec2( 12.9898, 78.233 ) ) ) * 43758.5453 );
        }

        float saturate( float x )
        {
            return clamp( x, 0.0, 1.0 );
        }

        void BokehLayer( inout vec3 color, vec2 p, vec3 c )
        {
            float wrap = 450.0;
            if ( mod( floor( p.y / wrap + 0.5 ), 2.0 ) == 0.0 )
            {
                p.x += wrap * 0.5;
            }

            vec2 p2 = mod( p + 0.5 * wrap, wrap ) - 0.5 * wrap;
            vec2 cell = floor( p / wrap + 0.5 );
            float cellR = Rand( cell );

            c *= fract( cellR * 3.33 + 3.33 );
            float radius = mix( 30.0, 70.0, fract( cellR * 7.77 + 7.77 ) );
            p2.x *= mix( 0.9, 1.1, fract( cellR * 11.13 + 11.13 ) );
            p2.y *= mix( 0.9, 1.1, fract( cellR * 17.17 + 17.17 ) );

            float sdf = Circle( p2, radius );
            float circle = 1.0 - smoothstep( 0.0, 1.0, sdf * 0.04 );
            float glow	 = exp( -sdf * 0.025 ) * 0.3 * ( 1.0 - circle );
            color += c * ( circle + glow );
        }

        float field(in vec3 p, float s) {
            float strength = 7. + .03 * log(1.e-6 + fract(sin(Time) * 4373.11));
            float accum = s/4.;
            float prev = 0.;
            float tw = 0.;
            for (int i = 0; i < 26; ++i) {
                float mag = dot(p, p);
                p = abs(p) / mag + vec3(-.5, -.4, -1.5);
                float w = exp(-float(i) / 7.);
                accum += w * exp(-strength * pow(abs(mag - prev), 2.2));
                tw += w;
                prev = mag;
            }
            return max(0., 5. * accum / tw - .7);
        }

        float field2(in vec3 p, float s) {
            float strength = 7. + .03 * log(1.e-6 + fract(sin(Time) * 4373.11));
            float accum = s/4.;
            float prev = 0.;
            float tw = 0.;
            for (int i = 0; i < 18; ++i) {
                float mag = dot(p, p);
                p = abs(p) / mag + vec3(-.5, -.4, -1.5);
                float w = exp(-float(i) / 7.);
                accum += w * exp(-strength * pow(abs(mag - prev), 2.2));
                tw += w;
                prev = mag;
            }
            return max(0., 5. * accum / tw - .7);
        }

        vec3 nrand3(vec2 co) {
            vec3 a = fract(cos(co.x*8.3e-3 + co.y)*vec3(1.3e5, 4.7e5, 2.9e5));
            vec3 b = fract(sin(co.x*0.3e-3 + co.y)*vec3(8.1e5, 1.0e5, 0.1e5));
            vec3 c = mix(a, b, 0.5);
            return c;
        }

        mat2 m(float a) {
            float c=cos(a), s=sin(a);
            return mat2(c, -s, s, c);
        }

        float map(vec3 p) {
            p.xz *= m(Time * 0.4);p.xy*= m(Time * 0.1);
            vec3 q = p * 2.0 + Time;
            return length(p+vec3(sin(Time * 0.7))) * log(length(p) + 1.0) + sin(q.x + sin(q.z + sin(q.y))) * 0.5 - 1.0;
        }

        void rise(out vec4 fragColor, in vec2 fragCoord) {
            vec2 a = fragCoord.xy / uSize.y - vec2(0.9, 0.5);
            vec3 cl = vec3(0.0);
            float d = 2.5;

            for (int i = 0; i <= 5; i++) {
                vec3 p = vec3(0, 0, 4.0) + normalize(vec3(a, -1.0)) * d;
                float rz = map(p);
                float f =  clamp((rz - map(p + 0.1)) * 0.5, -0.1, 1.0);
                vec3 l = vec3(0.1, 0.3, 0.4) + vec3(5.0, 2.5, 3.0) * f;
                cl = cl * l + smoothstep(2.5, 0.0, rz) * 0.6 * l;
                d += min(rz, 1.0);
            }

            fragColor = vec4(cl, 1.0);
        }

        void liquidbounce(out vec4 fragColor, in vec2 fragCoord) {
            vec2 uv = 2. * fragCoord.xy / uSize.xy - 1.;
            vec2 uvs = uv * uSize.xy / max(uSize.x, uSize.y);
            vec3 p = vec3(uvs / 4., 0) + vec3(1., -1.3, 0.);
            p += .2 * vec3(sin(Time / 16.), sin(Time / 12.), sin(Time / 128.));

            float freqs[4];
            freqs[0] = 0.02;
            freqs[1] = 0.07;
            freqs[2] = 0.15;
            freqs[3] = 0.30;

            float t = field(p, freqs[2]);
            float v = (1. - exp((abs(uv.x) - 1.) * 6.)) * (1. - exp((abs(uv.y) - 1.) * 6.));

            //Second Layer
            vec3 p2 = vec3(uvs / (4.+sin(Time*0.11)*0.2+0.2+sin(Time*0.15)*0.3+0.4), 1.5) + vec3(2., -1.3, -1.);
            p2 += 0.25 * vec3(sin(Time / 16.), sin(Time / 12.), sin(Time / 128.));
            float t2 = field2(p2, freqs[3]);
            vec4 c2 = mix(.4, 0.5, v) * vec4(0.8 * t2 * t2 * t2, 1.5 * t2 * t2, 1.5 * t2, t2);


            //Let's add some stars
            vec2 seed = p.xy * 2.0;
            seed = floor(seed * uSize.x);
            vec3 rnd = nrand3(seed);
            vec4 starcolor = vec4(pow(rnd.y, 40.0));

            //Second Layer
            vec2 seed2 = p2.xy * 2.0;
            seed2 = floor(seed2 * uSize.x);
            vec3 rnd2 = nrand3(seed2);
            starcolor += vec4(pow(rnd2.y, 40.0));

            fragColor = mix(freqs[3]-.3, 1., v) * vec4(1.5*freqs[2] * t * t* t, 1.2*freqs[1] * t * t, freqs[3]*t, 1.0)+c2+starcolor;
        }

        void kevin( out vec4 fragColor, in vec2 fragCoord )
        {
            vec2 uv = fragCoord.xy / uSize.xy;
            vec2 p = ( 2.0 * fragCoord - uSize.xy ) / uSize.x * 1000.0;

            // background
            vec3 color = mix( vec3( 0.3, 0.1, 0.3 ), vec3( 0.1, 0.4, 0.5 ), dot( uv, vec2( 0.2, 0.7 ) ) );

            float time = Time - 15.0;

            Rotate( p, 0.2 + time * 0.03 );
            BokehLayer( color, p + vec2( -50.0 * time +  0.0, 0.0  ), 3.0 * vec3( 0.4, 0.1, 0.2 ) );
            Rotate( p, 0.3 - time * 0.05 );
            BokehLayer( color, p + vec2( -70.0 * time + 33.0, -33.0 ), 3.5 * vec3( 0.6, 0.4, 0.2 ) );
            Rotate( p, 0.5 + time * 0.07 );
            BokehLayer( color, p + vec2( -60.0 * time + 55.0, 55.0 ), 3.0 * vec3( 0.4, 0.3, 0.2 ) );
            Rotate( p, 0.9 - time * 0.03 );
            BokehLayer( color, p + vec2( -25.0 * time + 77.0, 77.0 ), 3.0 * vec3( 0.4, 0.2, 0.1 ) );
            Rotate( p, 0.0 + time * 0.05 );
            BokehLayer( color, p + vec2( -15.0 * time + 99.0, 99.0 ), 3.0 * vec3( 0.2, 0.0, 0.4 ) );

            fragColor = vec4( color, 1.0 );
        }

        void main() {
            switch (Type) {
                case 0:
                rise(fragColor, gl_FragCoord.xy);
                break;
                case 1:
                liquidbounce(fragColor, gl_FragCoord.xy);
                break;
                case 2:
                kevin(fragColor, gl_FragCoord.xy);
                break;
                case 3:
                vec4 colorRise,colorLiquidBounce,colorKevin;
                kevin(colorKevin, gl_FragCoord.xy);
                liquidbounce(colorLiquidBounce, gl_FragCoord.xy);
                rise(colorRise, gl_FragCoord.xy);
                fragColor = (colorRise + colorLiquidBounce + colorKevin) / 3.0;
                break;
                case 4:
                fragColor = vec4(0,0,0,0);
                //rise(fragColor, gl_FragCoord.xy);
                break;
            }
        }
        """;
}
