package edu.vt.bi.google.util;

/**
* Allows timing of the execution of any block of code.
*/
public final class Stopwatch {

  /**
  * Start the stopwatch.
  *
  * @throws IllegalStateException if the stopwatch is already running.
  */
	/**
	 * This is the start for Stopwatch
	 * @param none
	 * @return void
	 */
  public void start(){
    if ( fIsRunning ) throw new IllegalStateException("Must stop before calling start again.");
    //reset both start and stop
    fStart = System.currentTimeMillis();
    fStop = 0;
    fIsRunning = true;
    fHasBeenUsedOnce = true;
  }

  /**
  * Stop the stopwatch.
  *
  * @throws IllegalStateException if the stopwatch is not already running.
  */
	/**
	 * This is the stop for Stopwatch
	 * @param none
	 * @return void
	 */
  public void stop() {
    if ( !fIsRunning ) throw new IllegalStateException("Cannot stop if not currently running.");
    fStop = System.currentTimeMillis();
    fIsRunning = false;
  }

  /**
   * Resume the stopwatch.
   *
   * @throws IllegalStateException if the stopwatch is already running.
   */
	/**
	 * This is the resume for Stopwatch
	 * @param none
	 * @return void
	 */
   public void resume() {
     if ( fIsRunning ) throw new IllegalStateException("Cannot resume if currently running.");
     long current = System.currentTimeMillis();
     fStart = current - (fStop - fStart);
     fStop = 0;
     fIsRunning = true;
     fHasBeenUsedOnce = true;
   }
  
  /**
  * Express the "reading" on the stopwatch.
  *
  * @throws IllegalStateException if the Stopwatch has never been used,
  * or if the stopwatch is still running.
  */
	/**
	 * This is the to String for Stopwatch
	 * @param none
	 * @return String result.toString()
	 */
  public String toString() {
    validateIsReadable();
    StringBuffer result = new StringBuffer();
    result.append(fStop - fStart);
    result.append(" ms");
    return result.toString();
  }

  /**
  * Express the "reading" on the stopwatch as a numeric type.
  *
  * @throws IllegalStateException if the Stopwatch has never been used,
  * or if the stopwatch is still running.
  */
	/**
	 * This is the to Value for Stopwatch
	 * @param none
	 * @return long fStop - fStart
	 */
  public long toValue() {
    validateIsReadable();
    return fStop - fStart;
  }

  // PRIVATE ////
  private long fStart;
  private long fStop;

  private boolean fIsRunning;
  private boolean fHasBeenUsedOnce;

  /**
  * Throws IllegalStateException if the watch has never been started,
  * or if the watch is still running.
  */
	/**
	 * This is the validate IsReadable for Stopwatch
	 * @param none
	 * @return void
	 */
  private void validateIsReadable() {
    if ( fIsRunning ) {
      String message = "Cannot read a stopwatch which is still running.";
      throw new IllegalStateException(message);
    }
    if ( !fHasBeenUsedOnce ) {
      String message = "Cannot read a stopwatch which has never been started.";
      throw new IllegalStateException(message);
    }
  }
}
