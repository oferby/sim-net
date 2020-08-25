package com.huawei.ibc.model.client;

public class GraphEdge extends GraphEntity {

    public GraphEdge() {
        super();
        super.setGroup(Group.EDGES);
    }

    public void setSource(String sourceId){
        super.addToData("source", sourceId);
    }

    public void setTraget(String targetId){
        super.addToData("target", targetId);
    }

    public String getSource(){
        return super.getDataEntry("source");
    }

    public String getTarget(){
        return super.getDataEntry("target");
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof GraphEdge)){
            return false;
        }

        return super.getDataEntry("source").equals(((GraphEdge) obj).getDataEntry("source")) &&
                super.getDataEntry("target").equals(((GraphEdge) obj).getDataEntry("target"));

    }

    @Override
    public int hashCode() {
        return this.getSource().hashCode() * 31 + this.getTarget().hashCode();
    }
}
