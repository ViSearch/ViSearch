# ViSearch

This is the repository of a research project **ViSearch**, which is a framework for weak consistency measurement of replicated data type. 

It is includes two components:

* [ViSearch Checker](https://github.com/AnonymousAccountForPaperReview/ViSearch/tree/main/checker): measures the consistency level of a history of replicated 
* [ViSearch Experiment](https://github.com/AnonymousAccountForPaperReview/ViSearch/tree/main/experiment): experiments that interact with database and generate histories
  * [Redis-Experiment](https://github.com/AnonymousAccountForPaperReview/Redis-CRDT-Experiment)
  * [Riak-Experiment](https://github.com/AnonymousAccountForPaperReview/Riak-CRDT-Experiment)

# Run ViSearch

If you want to measure consistency level of a history, see [ViSearch Checker](https://github.com/AnonymousAccountForPaperReview/ViSearch/tree/main/checker). 

If you want to adjust configuration of experiment, or generate new histories, see [ViSearch Experiment](https://github.com/AnonymousAccountForPaperReview/ViSearch/tree/main/experiment). 