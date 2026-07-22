package com.rrhh.saas.archivo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.almacenamiento")
public class PropiedadesAlmacenamiento {

    /** disco | s3 */
    private String proveedor = "disco";

    private String rutaBase = "./data/archivos";
    private int tamanoMaximoMb = 10;
    private PropiedadesS3 s3 = new PropiedadesS3();

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getRutaBase() {
        return rutaBase;
    }

    public void setRutaBase(String rutaBase) {
        this.rutaBase = rutaBase;
    }

    public int getTamanoMaximoMb() {
        return tamanoMaximoMb;
    }

    public void setTamanoMaximoMb(int tamanoMaximoMb) {
        this.tamanoMaximoMb = tamanoMaximoMb;
    }

    public long getTamanoMaximoBytes() {
        return (long) tamanoMaximoMb * 1024 * 1024;
    }

    public PropiedadesS3 getS3() {
        return s3;
    }

    public void setS3(PropiedadesS3 s3) {
        this.s3 = s3;
    }

    public static class PropiedadesS3 {
        private String endpoint = "";
        private String region = "us-east-1";
        private String bucket = "rrhh-archivos";
        private String accessKey = "";
        private String secretKey = "";
        private boolean pathStyle = true;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public boolean isPathStyle() {
            return pathStyle;
        }

        public void setPathStyle(boolean pathStyle) {
            this.pathStyle = pathStyle;
        }
    }
}
