package gmu.cs.cs477.courseproject;


import java.util.Comparator;

public class PostComparator implements Comparator<Post>{
    @Override
    public int compare(Post lhs, Post rhs) {
        return new Long(rhs.getTimestamp()).compareTo(lhs.getTimestamp());
    }

    @Override
    public boolean equals(Object object) {
        return false;
    }
}
