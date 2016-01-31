package app.aghaiand.popularmovies;

/**
 * Created by davidaghaian on 1/31/16.
 */
public class Movie
{
    String title;
    String overview;
    String vote;
    String releaseDate;


    public Movie()
    {

    }

    public Movie(String title,String overview,String vote,String releaseDate)
    {
        this.title = title;
        this.overview = overview;
        this.vote = vote;
        this.releaseDate = releaseDate;
    }

    String getTitle()
    {
        return title;
    }

    String getOverview()
    {
        return overview;
    }

    String getVote()
    {
        return vote;
    }

    String getReleaseDate()
    {
        return releaseDate;
    }

    void setTitle(String title)
    {
        this.title = title;
    }

    void setOverview(String overview)
    {
        this.overview = overview;
    }

    void setVote(String vote)
    {
        this.vote = vote;

    }

    void setReleaseDate(String releaseDate)
    {
        this.releaseDate = releaseDate;
    }
}
